package com.goteego.travel.controller;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.travel.dto.participation.ParticipationApplicationResponseDto;
import com.goteego.travel.dto.travel.BeforeTravelPostResponseDto;
import com.goteego.travel.dto.travel.TravelPostCreateRequest;
import com.goteego.travel.dto.travel.TravelPostResponseWrapper;
import com.goteego.travel.dto.travel.TravelPostUpdateRequest;
import com.goteego.travel.service.TravelPostService;
import com.goteego.user.domain.User;
import com.goteego.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 여행 게시글 목록 조회 (PostType별 다른 응답 구조)
     */
    @GetMapping
    public ResponseEntity<TravelPostResponseWrapper> getTravelPosts(
            @RequestParam(value = "postType", defaultValue = "BEFORE") String postType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        PostType currentPostType = PostType.valueOf(postType.toUpperCase());
        TravelPostResponseWrapper travelPosts;
        if (user == null) {
            // ✅ 비로그인 사용자 → 기본 목록 조회
            travelPosts = travelPostService.getTravelPostsDefault(currentPostType, page, size);
        } else {
            // ✅ 로그인 사용자 → 유사도 분석 기반 조회
            Long currentUserId = user.getId();
            travelPosts = travelPostService.getTravelPosts(currentPostType, page, size, currentUserId);
        }
        return ResponseEntity.ok(travelPosts);
    }


    // =====================================================준형======================================================= //

    /**
     * 여행 게시글 생성
     *
     * @param requestDto 게시글 생성 요청 데이터
     * @return 생성된 여행 게시글
     */
    @PostMapping(value = "/requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeforeTravelPostResponseDto> createTravelPost(
            @Valid @ModelAttribute TravelPostCreateRequest requestDto,
            @AuthenticationPrincipal User user) {

        User currentUser = userService.getUserById(user.getId());
        BeforeTravelPostResponseDto travelPostResponseDto = travelPostService.registerTravelPost(currentUser, requestDto);

        log.info("여행 게시글 생성 - travelPostId: {}, title: {}, userId: {}",
                travelPostResponseDto.getTravelPostId(), travelPostResponseDto.getTitle(), user.getId());

        return ResponseEntity.ok(travelPostResponseDto);
    }


    /**
     * 여행 게시글 참가 신청
     */
    @PostMapping("/requests/{travelPostId}")
    public ResponseEntity<ParticipationApplicationResponseDto> joinTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @AuthenticationPrincipal User user) {

        User currentUser = userService.getUserById(user.getId());
        ParticipationApplicationResponseDto response = travelPostService.joinTravelPost(travelPostId, currentUser);

        log.info("참가 신청 - travelPostId: {}, userId: {}, applicationId: {}", travelPostId, currentUser.getId(), response.getApplicationId());

        return ResponseEntity.ok(response);
    }


    // =====================================================준형======================================================= //


    // =====================================================재신======================================================= //

    /**
     * 여행 게시글 상세 조회
     *
     * @param travelPostId 게시글 ID
     * @return 여행 게시글 상세 정보
     */
    @GetMapping("/{travelPostId}")
    public ResponseEntity<TravelPost> getTravelPostDetail(@PathVariable("travelPostId") Long travelPostId) {
        TravelPost travelPost = travelPostService.getTravelPostDetail(travelPostId);

        log.info("여행 게시글 상세 조회 - travelPostId: {}, title: {}", travelPostId, travelPost.getTitle());

        return ResponseEntity.ok(travelPost);
    }


    /**
     * 여행 게시글 수정
     *
     * @param travelPostId 게시글 ID
     * @param requestDto   게시글 수정 요청 데이터
     * @return 수정된 여행 게시글
     */
    @PutMapping("/{travelPostId}")
    public ResponseEntity<TravelPost> updateTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @Valid @RequestBody TravelPostUpdateRequest requestDto,
            @AuthenticationPrincipal User user) {

        Long currentUserId = user.getId();

        // 새로운 DTO 기반 메서드 사용
        TravelPost travelPost = travelPostService.updateTravelPost(travelPostId, currentUserId, requestDto);

        log.info("여행 게시글 수정 - travelPostId: {}, title: {}, userId: {}",
                travelPostId, travelPost.getTitle(), currentUserId);

        return ResponseEntity.ok(travelPost);
    }

    /**
     * 여행 게시글 삭제
     *
     * @param travelPostId 게시글 ID
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{travelPostId}")
    public ResponseEntity<String> deleteTravelPost(
            @PathVariable("travelPostId") Long travelPostId,
            @AuthenticationPrincipal User user) {

        Long currentUserId = user.getId();

        String result = travelPostService.deleteTravelPost(travelPostId, currentUserId);

        log.info("여행 게시글 삭제 - travelPostId: {}, userId: {}", travelPostId, currentUserId);

        return ResponseEntity.ok(result);
    }


    // =====================================================재신======================================================= //


} 