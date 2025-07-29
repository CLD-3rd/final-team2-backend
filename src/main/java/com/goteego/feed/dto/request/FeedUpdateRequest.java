package com.goteego.feed.dto.request;

import com.goteego.global.domain.enumerate.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 피드 수정 요청 DTO
 * 클라이언트로부터 피드 수정 요청을 받을 때 사용되는 데이터 전송 객체
 */
@Getter
@NoArgsConstructor
public class FeedUpdateRequest {
    
    @NotBlank(message = "피드 제목은 필수입니다.")
    @Size(max = 200, message = "피드 제목은 200자를 초과할 수 없습니다.")
    private String title;
    
    @NotBlank(message = "피드 내용은 필수입니다.")
    private String content;
    
    private String imageUrl;
    
    private Location location;
    
    private Boolean badgeRequest;
    
    private String deleteImageUrl;
    
    // Builder 패턴 적용
    public static FeedUpdateRequestBuilder builder() {
        return new FeedUpdateRequestBuilder();
    }
    
    public static class FeedUpdateRequestBuilder {
        private String title;
        private String content;
        private String imageUrl;
        private Location location;
        private Boolean badgeRequest;
        private String deleteImageUrl;
        
        public FeedUpdateRequestBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public FeedUpdateRequestBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public FeedUpdateRequestBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }
        
        public FeedUpdateRequestBuilder location(Location location) {
            this.location = location;
            return this;
        }
        
        public FeedUpdateRequestBuilder badgeRequest(Boolean badgeRequest) {
            this.badgeRequest = badgeRequest;
            return this;
        }
        
        public FeedUpdateRequestBuilder deleteImageUrl(String deleteImageUrl) {
            this.deleteImageUrl = deleteImageUrl;
            return this;
        }
        
        public FeedUpdateRequest build() {
            FeedUpdateRequest request = new FeedUpdateRequest();
            request.title = this.title;
            request.content = this.content;
            request.imageUrl = this.imageUrl;
            request.location = this.location;
            request.badgeRequest = this.badgeRequest;
            request.deleteImageUrl = this.deleteImageUrl;
            return request;
        }
    }
} 