package com.goteego.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FeedCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "위치는 필수입니다.")
    private String location;
    @NotNull(message = "배지 요청 여부는 필수입니다.")
    private Boolean badgeRequest;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private MultipartFile image;
}
