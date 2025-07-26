package com.goteego.travel.dto.travel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelPostUpdateRequest {
    private Long postId;
    private String title;
    private String content;
    private LocalDate startTime;
    private LocalDate endTime;
    private String imageUrl;
    private Integer recuitLimit;
    private String postType;
    private Boolean isAddRecruit;
}