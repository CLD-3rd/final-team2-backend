package com.goteego.badge.service;

import com.goteego.badge.domain.Badge;
import com.goteego.badge.domain.LandmarkBadgeRequest;
import com.goteego.badge.domain.UserBadge;
import com.goteego.badge.domain.enumerate.BadgeCategory;
import com.goteego.badge.domain.enumerate.BadgeCode;
import com.goteego.badge.domain.enumerate.BadgeStatus;
import com.goteego.badge.dto.BadgeRequestsResponse;
import com.goteego.badge.dto.BadgeResponse;
import com.goteego.badge.repository.BadgeRepository;
import com.goteego.badge.repository.LandmarkBadgeRequestReposiroty;
import com.goteego.badge.repository.UserBadgeRepository;
import com.goteego.feed.domain.Feed;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import com.goteego.global.s3.S3Directory;
import com.goteego.global.s3.S3Service;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final FeedRepository feedRepository;
    private final LandmarkBadgeRequestReposiroty landmarkBadgeRequestReposiroty;
    private final S3Service s3Service;

    public List<BadgeResponse> getBadgesByUserId(Long userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        if (userBadges.isEmpty()) {
            throw new NotFoundException(ErrorCode.NOT_FOUND_BADGE);
        }

        return userBadges.stream().map(userBadge -> BadgeResponse.from(userBadge.getBadge())).toList();
    }

    /**
     * 뱃지 데이터 생성 -> api호출
     */
    public void uploadToS3() {
        if (badgeRepository.count() == 0) {
            createBadgeData();
        }
    }

    /**
     * 서비스가 제공하는 뱃지 데이터 생성 -> 서버 재시작 용
     */
    @PostConstruct
    public void initBadgeData() {
        // 없으면 생성
        if (badgeRepository.count() == 0) {
            createBadgeData();
        }
    }

    public void createBadgeData() {
        List<BadgeCode> badgeCodes = List.of(
                BadgeCode.LANDMARK_EIFFEL,
                BadgeCode.LANDMARK_HALLA,
                BadgeCode.LANDMARK_HAESHA,
                BadgeCode.LANDMARK_GWANGHWAMUN,
                BadgeCode.LANDMARK_ANMOKCAFE,
                BadgeCode.LANDMARK_EXPOBRIDGE,
                BadgeCode.LANDMARK_U_SQUARE,
                BadgeCode.LANDMARK_BULGUKSA,
                BadgeCode.LANDMARK_SEORAKSAN,
                BadgeCode.LANDMARK_JEONJUHANOK);

        List<String> googleFileIds = List.of(
                "1A4_upRjZwtPNI19J2QS5_RZ3N6dIToug",
                "1oQXqa5fJp4QHEqNqxT-BOhsx6gkNfmVr",
                "1l_mCCvFwhcLNBVmAJJQg6DwsH7F9khll",
                "1pFQSAN18-nVdgnViKnRmp3hJbJ6UBQzC",
                "14f8nNZi90LCsVSa64ZXvKeTfydnGj_CV",
                "1hP_6E62GDX_is0RmUi-jlA97aUMbqC3_",
                "1hUZmItVGoyCbDhw75eT-lVBcaX8t5mRm",
                "1N2J1lhkpuCzghiA6HCGNKLZL1mIv7ZsO",
                "11l8Mveo4wre1uY5Um0wSuyuMng-B3UVG",
                "1SywQdxcR12_VFl24jLk81oGq4nn87YM6"
        );


        for (int i = 0; i < badgeCodes.size(); i++) {
            try (InputStream inputStream = downloadImageFromGoogleDrive(googleFileIds.get(i))) {
                String fileName = badgeCodes.get(i).name().toLowerCase() + ".png";
                String key = S3Directory.BADGES + "/" + fileName;

                // InputStream 크기 측정
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                inputStream.transferTo(baos);
                byte[] bytes = baos.toByteArray();

                try (InputStream uploadStream = new ByteArrayInputStream(bytes)) {
                    String uploadedImageUrl = s3Service.uploadFile(uploadStream, bytes.length, "image/png", key);

                    Badge badge = Badge.builder()
                            .category(BadgeCategory.LANDMARK)
                            .code(badgeCodes.get(i))
                            .imgUrl(uploadedImageUrl)
                            .build();

                    badgeRepository.save(badge);
                }

            } catch (IOException e) {
                log.error("이미지 다운로드 또는 업로드 실패: " + badgeCodes.get(i), e);
            }
        }
    }


    public InputStream downloadImageFromGoogleDrive(String fileId) throws IOException {
        String downloadUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        } else {
            throw new IOException("Failed to download file from Google Drive. HTTP code: " + responseCode);
        }
    }


    @Transactional
    public void processBadgeByApproval(Long feedId, boolean approved) {

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));


        if (feed.getLocation() == null) return;

        Badge badge;
        try {
            badge = getBadgeByFeed(feed.getId(), true);
        } catch (IllegalArgumentException e) {
            if (approved) throw e;
            else return;
        }

        boolean hasBadge = userBadgeRepository.existsByUserAndBadge(feed.getAuthor(), badge);

        if (approved) {
            if (!hasBadge) {
                UserBadge userBadge = UserBadge.builder().user(feed.getAuthor()).badge(badge).build();
                userBadgeRepository.save(userBadge);
            }
            LandmarkBadgeRequest request = landmarkBadgeRequestReposiroty.findByFeed(feed).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_BADGE_REQUEST));
            request.approve();


        } else {
            if (hasBadge) {
                userBadgeRepository.deleteByUserAndBadge(feed.getAuthor(), badge);
            }
            landmarkBadgeRequestReposiroty.findByFeed(feed).ifPresent(LandmarkBadgeRequest::reject);
        }
    }


    private Badge getBadgeByFeed(Long feedId, boolean useDefaultIfInvalid) {

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));

        Location location = feed.getLocation();

        BadgeCode badgeCode = location.getBadgeCode(); // 안전한 변환


        if (badgeCode == null && useDefaultIfInvalid) {
            badgeCode = BadgeCode.LANDMARK_EIFFEL;
        }
        //뱃지 초기데이터 없으면 걸림
        return badgeRepository.findByCode(badgeCode).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_BADGE_CODE));
    }


    @Transactional
    public List<BadgeRequestsResponse> getBadgesRequests() {
        List<LandmarkBadgeRequest> requests = landmarkBadgeRequestReposiroty.findAllByStatus(BadgeStatus.PENDING);

        return requests.stream().map(req -> BadgeRequestsResponse.of(req.getId(), req.getFeed().getAuthor().getId(), req.getFeed().getId(), req.getStatus().name())).collect(Collectors.toList());
    }

    /**
     * 사용자 프로필에 표시할 뱃지를 업데이트하는 메서드
     *
     * @param userId   사용자 ID
     * @param badgeIds 표시할 뱃지 ID 목록
     */
    @Transactional
    public void updateDisplayedBadges(Long userId, List<Long> badgeIds) {
        // 1. 사용자 뱃지 조회
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        if (userBadges.isEmpty()) {
            throw new NotFoundException(ErrorCode.USER_BADGE_NOT_FOUND);
        }

        // 2. 모든 뱃지 표시 상태 초기화
        userBadges.forEach(userBadge -> userBadge.setDisplay(false));

        // 3. 선택된 뱃지 ID들에 해당하는 항목만 true
        userBadges.stream()
                .filter(userBadge -> badgeIds.contains(userBadge.getBadge().getId()))
                .forEach(userBadge -> userBadge.setDisplay(true));
    }
    /**
     * 사용자 마이페이지에서 뱃지 선택하는 메서드
     * @param badgeId
     * @return
     */
    @Transactional
    public List<BadgeResponse> setDisplayedBadge(Long badgeId, Long userId) {

        // 1. 해당 유저의 모든 뱃지를 가져온다
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        // 2. true로 설정된 뱃지가 있다면 모두 false로 변경
        userBadges.stream()
                .filter(UserBadge::isDisplay)
                .forEach(ub -> ub.setDisplay(false));

        // 3. badgeId에 해당하는 뱃지를 true로 설정
        UserBadge selectedBadge = userBadgeRepository.findByUser_IdAndBadge_Id(userId, badgeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_BADGE));


        selectedBadge.setDisplay(true);


        // 4. 변경된 내용 DB에 반영
        userBadgeRepository.saveAll(userBadges);
        userBadgeRepository.save(selectedBadge);


        // 5. BadgeResponse 반환
        return userBadges.stream()
                .map(ub -> BadgeResponse.from(ub.getBadge()))
                .collect(Collectors.toList());
    }

}
