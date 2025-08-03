package com.goteego.travelPost.dto.travel;

import com.goteego.global.domain.enumerate.Location;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TravelPostRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String content;

    @NotBlank(message = "지역은 필수입니다")
    private String location; // String으로 받아서 Location enum으로 변환

    private LocalDate startTime;
    private LocalDate endTime;

    private MultipartFile image;

    private Integer recruitLimit;
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
