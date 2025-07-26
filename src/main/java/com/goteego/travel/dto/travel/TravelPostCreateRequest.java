package com.goteego.travel.dto.travel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelPostCreateRequest {
    private String title;
    private String content;
    private LocalDate startTime;
    private LocalDate endTime;
    private String imageUrl;
    private Integer recuitLimit;
    private String postType;
    private Boolean isAddRecruit;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDate getStartTime() { return startTime; }
    public void setStartTime(LocalDate startTime) { this.startTime = startTime; }

    public LocalDate getEndTime() { return endTime; }
    public void setEndTime(LocalDate endTime) { this.endTime = endTime; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getRecuitLimit() { return recuitLimit; }
    public void setRecuitLimit(Integer recuitLimit) { this.recuitLimit = recuitLimit; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public Boolean getIsAddRecruit() { return isAddRecruit; }
    public void setIsAddRecruit(Boolean isAddRecruit) { this.isAddRecruit = isAddRecruit; }
}
