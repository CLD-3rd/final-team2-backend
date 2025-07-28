package com.goteego.review.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewTargetDto {
    private Long postId;
    private String postTitle;
    private Long targetUserId;
    private String targetNickname;
    private String profileImgUrl;

}