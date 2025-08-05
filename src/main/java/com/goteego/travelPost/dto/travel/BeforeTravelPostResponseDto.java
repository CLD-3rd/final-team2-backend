package com.goteego.travelPost.dto.travel;

import com.goteego.global.dto.Author;
import com.goteego.travelPost.domain.TravelPost;
import com.goteego.travelPost.domain.enumerate.ProgressStatus;
import com.goteego.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사전동행모집글 응답 DTO (BEFORE)
 * API 응답 시 사용되는 데이터 전송 객체
 */
import com.goteego.global.dto.Author;
import com.goteego.travelPost.domain.TravelPost;
import com.goteego.travelPost.domain.enumerate.ProgressStatus;
import com.goteego.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate; // LocalDate 임포트 추가

/**
 * 사전동행모집글 응답 DTO (BEFORE)
 * API 응답 시 사용되는 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class BeforeTravelPostResponseDto {

    private Long travelPostId;
    private String title;
    private String content;
    private String location;
    private Long viewCount;
    private String startTime;
    private String endTime;
    private Author author;
    private Integer participants;
    private Integer maxParticipants;
    private String imageUrl;
    private String createdAt;
    private ProgressStatus status; // 이 필드를 채워줄 예정

    /**
     * TravelPost 엔티티를 DTO로 변환
     */
    public static BeforeTravelPostResponseDto from(TravelPost travelPost, User user, Integer approvedParticipantCount, Long viewCount) {

        // 1. 현재 날짜와 여행 시작일/종료일 가져오기
        ProgressStatus calculatedStatus = null;

        LocalDate today = LocalDate.now();
        LocalDate startTime = travelPost.getStartTime().atStartOfDay().toLocalDate();
        LocalDate endTime = travelPost.getEndTime().atStartOfDay().toLocalDate();

        // 2. 게시글 상태 지정
        if (today.isBefore(startTime)) {
            calculatedStatus = ProgressStatus.UPCOMING; // 오늘이 시작일 전이면 "예정"
        } else if (today.isAfter(endTime)) {
            calculatedStatus = ProgressStatus.COMPLETED; // 오늘이 종료일 후면 "완료"
        } else {
            calculatedStatus = ProgressStatus.ONGOING; // 그 외(시작일과 종료일 사이)는 "진행중"
        }

        // 3. DTO 반환
        return BeforeTravelPostResponseDto.builder()
                .travelPostId(travelPost.getId())
                .title(travelPost.getTitle())
                .content(travelPost.getContent())
                .location(travelPost.getLocation().name())
                .viewCount(viewCount)
                .startTime(travelPost.getStartTime().toString())
                .endTime(travelPost.getEndTime().toString())
                .author(Author.from(user))
                .participants(approvedParticipantCount != null ? approvedParticipantCount : 0)
                .maxParticipants(travelPost.getRecruitLimit())
                .imageUrl(travelPost.getImageUrl())
                .createdAt(travelPost.getCreatedAt().toString())
                .status(calculatedStatus) // 계산된 상태를 DTO에 설정
                .build();
    }
}