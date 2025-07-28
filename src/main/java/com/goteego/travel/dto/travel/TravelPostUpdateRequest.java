package com.goteego.travel.dto.travel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelPostUpdateRequest {
    
    // postId는 경로 변수에서 받으므로 제거
    // private Long postId;
    
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    @NotBlank(message = "지역은 필수입니다")
    private String location;
    
    @NotNull(message = "시작 날짜는 필수입니다")
    private LocalDate startTime;
    
    @NotNull(message = "종료 날짜는 필수입니다")
    private LocalDate endTime;
    
    private String imageUrl;
    
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다")
    private Integer recruitLimit;
    
    @Pattern(regexp = "^(BEFORE|NOW)$", message = "게시글 타입은 BEFORE 또는 NOW여야 합니다")
    private String postType;
    
    private Boolean isAddRecruit;
}