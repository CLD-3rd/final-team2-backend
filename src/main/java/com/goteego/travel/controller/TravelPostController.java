package com.goteego.travel.controller;

import com.goteego.travel.domain.TravelPost;
import com.goteego.travel.domain.enumerate.PostType;
import com.goteego.travel.dto.participation.ParticipationApplicationResponseDto;
import com.goteego.travel.dto.travel.*;
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

import java.net.URI;


@Slf4j
@RestController
@RequestMapping("/api/travel-posts")
@RequiredArgsConstructor
public class TravelPostController {

    private final TravelPostService travelPostService;
    private final UserService userService;

    /**
     * 여행 게시글 목록 조회 (PostType별 다른 응답 구조)
     *
     * @param postType
     * @param page
     * @param size
     * @param user
     * @return
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
     * 여행 게시글 생성
     *
     * @param requestDto
     * @param user
     * @return
     */
    @PostMapping(value = "/requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BeforeTravelPostResponseDto> createTravelPost(
            @Valid @ModelAttribute TravelPostCreateRequest requestDto,
            @AuthenticationPrincipal User user) {

        User currentUser = userService.getUserById(user.getId());
        Long newTravelPostId = travelPostService.registerTravelPost(currentUser, requestDto);

        log.info("여행 게시글 생성 - travelPostId: {}, userId: {}",
                newTravelPostId, user.getId());

        URI location = URI.create("/api/travel-posts/" + newTravelPostId);

        return ResponseEntity.created(location).build();
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
    public ResponseEntity<TravelPostDetailResponseDto> getTravelPostDetail(@PathVariable("travelPostId") Long travelPostId) {
        TravelPostDetailResponseDto travelPostDetail = travelPostService.getTravelPostDetail(travelPostId);

        log.info("여행 게시글 상세 조회 - travelPostId: {}, title: {}", travelPostId, travelPostDetail.getTitle());

        return ResponseEntity.ok(travelPostDetail);
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
            @Valid @ModelAttribute TravelPostUpdateRequest requestDto,
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