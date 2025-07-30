package com.goteego.travel.controller;

import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.travel.dto.travel.TravelPostCreateRequest;
import com.goteego.travel.dto.travel.TravelPostDetailResponseDto;
import com.goteego.travel.dto.travel.TravelPostResponseWrapper;
import com.goteego.travel.dto.travel.TravelPostUpdateRequest;
import com.goteego.travel.service.TravelPostService;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/travel-posts")
@RequiredArgsConstructor
public class TravelPostController {

    private final TravelPostService travelPostService;
    private final UserService userService;

    /**
     * 여행 게시글 목록 조회
     * <p>
     * PostType(BEFORE/NOW)에 따라 다른 응답 구조를 반환하며,
     * 페이지네이션을 지원합니다.
     *
     * @param postType 조회할 게시글 타입 (BEFORE/NOW)
     * @param page     페이지 번호 (기본값 0)
     * @param size     페이지 크기 (기본값 10)
     * @param user     로그인 사용자 (비로그인 시 null)
     * @return TravelPostResponseWrapper (게시글 목록 + 페이지 정보)
     */
    @GetMapping
    public ResponseEntity<TravelPostResponseWrapper> getTravelPosts(
            @RequestParam(value = "postType", defaultValue = "BEFORE") String postType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        PostType currentPostType = PostType.valueOf(postType.toUpperCase());
        TravelPostResponseWrapper travelPosts = travelPostService.getTravelPosts(currentPostType, page, size, user);

        return ResponseEntity.ok(travelPosts);
    }

    /**
     * 여행 게시글 상세 조회
     * <p>
     * 게시글 ID를 기준으로 여행 게시글의 상세 정보를 반환합니다.
     *
     * @param travelPostId 상세 조회할 게시글 ID
     * @return TravelPostDetailResponseDto (게시글 상세 정보)
     */
    @GetMapping("/{travelPostId}")
    public ResponseEntity<TravelPostDetailResponseDto> getTravelPostDetail(
            @PathVariable("travelPostId") Long travelPostId) {

        TravelPostDetailResponseDto travelPostDetail = travelPostService.getTravelPostDetail(travelPostId);

        log.info("여행 게시글 상세 조회 - travelPostId: {}, title: {}", travelPostId, travelPostDetail.getTitle());

        return ResponseEntity.ok(travelPostDetail);
    }

    /**
     * 여행 게시글 생성
     * <p>
     * Multipart/Form-Data 방식으로 게시글 정보를 받아 새로운 여행 게시글을 생성합니다.
     * 응답은 생성 성공 여부만 반환합니다.
     *
     * @param requestDto 게시글 생성 요청 데이터
     * @param user       로그인 사용자
     * @return 201 Created (Body 없음)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createTravelPost(
            @Valid @ModelAttribute TravelPostCreateRequest requestDto,
            @AuthenticationPrincipal User user) {

        User currentUser = userService.getUserById(user.getId());
        Long newTravelPostId = travelPostService.registerTravelPost(currentUser, requestDto);

        log.info("여행 게시글 생성 - travelPostId: {}, userId: {}",
                newTravelPostId, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 여행 게시글 수정
     * <p>
     * 기존 여행 게시글을 수정합니다.
     * 응답은 성공 여부만 반환하며, 최신 데이터는 별도 GET 요청으로 확인합니다.
     *
     * @param travelPostId 수정할 게시글 ID
     * @param requestDto   수정 요청 데이터
     * @param user         로그인 사용자
     * @return 204 No Content
     */
    @PutMapping("/{travelPostId}")
    public ResponseEntity<Void> updateTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @Valid @ModelAttribute TravelPostUpdateRequest requestDto,
            @AuthenticationPrincipal User user) {

        Long currentUserId = user.getId();
        travelPostService.updateTravelPost(travelPostId, currentUserId, requestDto);

        log.info("여행 게시글 수정 - travelPostId: {}, userId: {}", travelPostId, currentUserId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 여행 게시글 삭제
     * <p>
     * 게시글 ID를 기준으로 여행 게시글을 삭제합니다.
     *
     * @param travelPostId 삭제할 게시글 ID
     * @param user         로그인 사용자
     * @return 204 No Content
     */
    @DeleteMapping("/{travelPostId}")
    public ResponseEntity<Void> deleteTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @AuthenticationPrincipal User user) {

        Long currentUserId = user.getId();
        travelPostService.deleteTravelPost(travelPostId, currentUserId);

        log.info("여행 게시글 삭제 - travelPostId: {}, userId: {}", travelPostId, currentUserId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 여행 게시글 참가 신청
     * <p>
     * 특정 여행 게시글에 대한 참가 신청을 처리합니다.
     *
     * @param travelPostId 참가 신청할 게시글 ID
     * @param user         로그인 사용자
     * @return 200 OK (Body 없음)
     */
    @PostMapping("/{travelPostId}/participations")
    public ResponseEntity<Void> joinTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @AuthenticationPrincipal User user) {

        User currentUser = userService.getUserById(user.getId());
        travelPostService.joinTravelPost(travelPostId, currentUser);

        log.info("참가 신청 - travelPostId: {}, userId: {}",
                travelPostId, currentUser.getId());

        return ResponseEntity.ok().build();
    }
}