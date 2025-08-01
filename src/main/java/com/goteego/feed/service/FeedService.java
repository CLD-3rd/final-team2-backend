package com.goteego.feed.service;

import com.goteego.badge.domain.LandmarkBadgeRequest;
import com.goteego.badge.domain.enumerate.BadgeStatus;
import com.goteego.badge.repository.LandmarkBadgeRequestReposiroty;
import com.goteego.feed.domain.Feed;
import com.goteego.feed.dto.request.FeedPostRequest;
import com.goteego.feed.dto.response.FeedCommentResponse;
import com.goteego.feed.dto.response.FeedDetailResponse;
import com.goteego.feed.dto.response.FeedListResponse;
import com.goteego.feed.dto.response.FeedResponse;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.global.dto.PageInfo;
import com.goteego.global.dto.SearchCondition;
import com.goteego.global.error.exception.BusinessException;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.global.error.exception.UnauthorizedAccessException;
import com.goteego.global.s3.S3Directory;
import com.goteego.global.s3.S3Service;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 피드 서비스 클래스
 * 피드 관련 비즈니스 로직을 처리하는 서비스 계층
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserService userService;
    private final FeedCommentService feedCommentService;
    private final LandmarkBadgeRequestReposiroty landmarkBadgeRequestReposiroty;
    private final S3Service s3Service;

    /**
     * 피드 목록 조회
     *
     * @param page      페이지 번호 (0부터 시작)
     * @param size      한 페이지에 표시할 피드 개수
     * @param condition 검색 및 정렬 조건을 담고 있는 SearchCondition 객체
     * @return FeedListResponse 피드 목록과 페이지 정보가 포함된 응답 객체
     */
    @Transactional(readOnly = true)
    public FeedListResponse getFeeds(int page, int size, SearchCondition condition) {
        // ✅ 정렬 조건 설정
        Pageable pageable = PageRequest.of(page, size, getSortOption(condition.sort()));

        // ✅ location 검증 및 변환
        Location locationEnum = null;
        if (condition.location() != null && !condition.location().isBlank()) {
            try {
                locationEnum = Location.fromDisplayName(condition.location());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_LOCATION);
            }
        }

        // ✅ 조건에 맞는 피드 조회
        Page<Feed> feedPage = feedRepository.getFeedsWithCondition(
                condition.title(),
                condition.author(),
                locationEnum,
                pageable);
        PageInfo pageInfo = PageInfo.from(feedPage);

        // ✅ 조회된 피드를 FeedResponse DTO로 변환
        List<FeedResponse> feedResponseList = feedPage.getContent().stream()
                .map(FeedResponse::from).toList();

        // ✅ 최종 응답 DTO 생성 및 반환
        return FeedListResponse.builder()
                .feeds(feedResponseList)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * 피드 상세 정보 조회
     * 주어진 피드 ID에 해당하는 피드를 조회하고, 관련된 댓글 목록을 가져와 피드의 상세 정보를 반환합니다.
     * 또한 피드 조회 시 조회수를 증가시킵니다.
     *
     * @param feedId 조회할 피드의 ID
     * @return 피드의 상세 정보와 관련된 댓글 목록을 포함한 `FeedDetailResponse` 객체
     */
    @Transactional
    public FeedDetailResponse getFeedDetail(Long feedId, User user) {
        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));

        // 코멘트 목록 조회
        List<FeedCommentResponse> comments = feedCommentService.getComments(feedId, user);

        // 조회수 증가
        feed.incrementViewCount();

        // FeedDetailResponse로 변환하여 반환
        return FeedDetailResponse.from(feed, comments);
    }

    /**
     * 피드 생성
     *
     * @param userId  현재 피드를 생성하는 사용자의 ID
     * @param request 피드 생성에 필요한 데이터 (제목, 내용, 이미지, 위치 등)
     * @return 생성된 피드의 ID
     */
    @Transactional
    public Long createFeed(Long userId, FeedPostRequest request) {
        // 사용자 조회 - 현재 피드를 생성하려는 사용자 정보 조회
        User currentUser = userService.getUserById(userId);

        // 이미지 S3에 업로드 처리 - 이미지가 있으면 업로드 후 URL 반환
        String uploadedImageUrl = s3Service.uploadFile(request.getImage(), S3Directory.FEEDS, userId);

        // 피드 엔티티 생성 - 피드의 제목, 내용, 이미지 URL, 위치, 배지 요청 여부 등 설정
        Feed newFeed = Feed.builder()
                .author(currentUser)
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(uploadedImageUrl)
                .location(Location.fromString(request.getLocation()))
                .badgeRequest(request.getBadgeRequest())
                .build();

        // 피드 저장 - 생성된 피드를 데이터베이스에 저장하고 저장된 피드 반환
        Feed savedFeed = feedRepository.save(newFeed);

        // 생성된 피드의 ID 반환
        return savedFeed.getId();
    }

    /**
     * 피드 수정
     *
     * @param feedId  수정할 피드의 ID
     * @param userId  수정 요청을 보낸 사용자의 ID (권한 검증용)
     * @param request 피드 수정에 필요한 데이터 (제목, 내용, 이미지, 위치, 배지 요청 여부)
     */
    @Transactional
    public void updateFeed(Long feedId, Long userId, FeedPostRequest request) {
        // 피드 조회: feedId에 해당하는 피드를 데이터베이스에서 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));

        // 작성자 권한 검증: 요청한 사용자가 해당 피드의 작성자인지 확인
        if (!feed.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedAccessException(ErrorCode.UNAUTHORIZED_FEED_UPDATE);
        }

        // 이미지 URL 업데이트: 이미지는 있을 경우에만 새로 업로드하고, 없으면 기존 이미지 URL을 그대로 유지
        String imageUrl = feed.getImageUrl(); // 기본값: 기존 이미지 유지
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            // 기존 이미지 삭제
            if (imageUrl != null) {
                String oldKey = S3Service.extractKeyFromUrl(imageUrl, S3Directory.FEEDS);
                s3Service.deleteFile(oldKey);
            }
            // 새 이미지 업로드
            imageUrl = s3Service.uploadFile(request.getImage(), S3Directory.FEEDS, userId);
        }

        // 피드 정보 수정: 수정된 데이터를 기존 피드 엔티티에 반영
        feed.update(
                request.getTitle(),
                request.getContent(),
                imageUrl,
                Location.fromString(request.getLocation()),
                request.getBadgeRequest()
        );
    }

    /**
     * 피드 삭제
     *
     * @param feedId 삭제할 피드의 ID
     * @param userId 삭제 요청을 보낸 사용자의 ID (권한 검증용)
     */
    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        // 피드 조회: feedId에 해당하는 피드를 데이터베이스에서 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));

        // 작성자 권한 검증: 요청한 사용자가 해당 피드의 작성자인지 확인
        if (!feed.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedAccessException(ErrorCode.UNAUTHORIZED_FEED_UPDATE);
        }

        // 이미지 삭제 (S3)
        String imageKey = S3Service.extractKeyFromUrl(feed.getImageUrl(), S3Directory.FEEDS);
        s3Service.deleteFile(imageKey);

        // 피드 삭제: 작성자 권한이 검증된 후, 피드를 삭제
        feedRepository.delete(feed);
    }

    /**
     * 뱃지요청 저장 매서드
     *
     * @param feed
     */
    @Transactional
    public void requestBadgeByFeed(Feed feed) {
        //뱃지요청
        if (Boolean.TRUE.equals(feed.getBadgeRequest())) {
            LandmarkBadgeRequest request = LandmarkBadgeRequest.builder()
                    .feed(feed)
                    .status(BadgeStatus.PENDING)
                    .build();
            landmarkBadgeRequestReposiroty.save(request);
        }
    }

    // =====================================================내부 로직======================================================= //

    /**
     * 게시글 정렬 옵션 처리
     *
     * @param sort 정렬 기준 (recent / view), 기본값: view
     * @return Sort 객체
     */
    private Sort getSortOption(String sort) {
        return switch (sort) {
            case "recent" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "viewCount");
        };
    }
}