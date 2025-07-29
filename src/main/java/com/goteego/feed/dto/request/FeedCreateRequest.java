package com.goteego.feed.dto.request;

import com.goteego.global.domain.enumerate.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 피드 생성 요청 DTO
 * 클라이언트로부터 피드 생성 요청을 받을 때 사용되는 데이터 전송 객체
 */
@Getter
@NoArgsConstructor
public class FeedCreateRequest {
    
    @NotBlank(message = "피드 제목은 필수입니다.")
    @Size(max = 200, message = "피드 제목은 200자를 초과할 수 없습니다.")
    private String title;
    
    @NotBlank(message = "피드 내용은 필수입니다.")
    private String content;
    
    private String imageUrl;
    
    private Location location;
    
    private Boolean badgeRequest = false;
    
    // Builder 패턴 적용
    public static FeedCreateRequestBuilder builder() {
        return new FeedCreateRequestBuilder();
    }
    
    public static class FeedCreateRequestBuilder {
        private String title;
        private String content;
        private String imageUrl;
        private Location location;
        private Boolean badgeRequest = false;
        
        public FeedCreateRequestBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public FeedCreateRequestBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public FeedCreateRequestBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }
        
        public FeedCreateRequestBuilder location(Location location) {
            this.location = location;
            return this;
        }
        
        public FeedCreateRequestBuilder badgeRequest(Boolean badgeRequest) {
            this.badgeRequest = badgeRequest != null ? badgeRequest : false;
            return this;
        }
        
        public FeedCreateRequest build() {
            FeedCreateRequest request = new FeedCreateRequest();
            request.title = this.title;
            request.content = this.content;
            request.imageUrl = this.imageUrl;
            request.location = this.location;
            request.badgeRequest = this.badgeRequest;
            return request;
        }
    }
} 