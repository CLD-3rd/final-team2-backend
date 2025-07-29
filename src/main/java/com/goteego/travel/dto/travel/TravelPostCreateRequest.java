package com.goteego.travel.dto.travel;

import com.goteego.global.domain.enumerate.Location;
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
public class TravelPostCreateRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    @NotBlank(message = "지역은 필수입니다")
    private String location; // String으로 받아서 Location enum으로 변환
    
    @NotNull(message = "시작 날짜는 필수입니다")
    private LocalDate startTime;
    
    @NotNull(message = "종료 날짜는 필수입니다")
    private LocalDate endTime;
    
    private String imageUrl;
    
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다")
    private Integer recruitLimit; // recuitLimit -> recruitLimit로 오타 수정
    
    @Pattern(regexp = "^(BEFORE|NOW)$", message = "게시글 타입은 BEFORE 또는 NOW여야 합니다")
    private String postType;
    
    private Boolean isAddRecruit;
    
    /**
     * String location을 Location enum으로 변환
     * null 체크 및 예외 처리를 포함한 안전한 변환
     */
    public Location getLocationAsEnum() {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("지역 정보가 비어있습니다.");
        }
        
        try {
            // HTML에서 이미 대문자로 전송되므로 trim()만 수행
            return Location.valueOf(location.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 지역입니다: " + location + 
                ". 지원되는 지역: " + java.util.Arrays.toString(Location.values()));
        }
    }
}
