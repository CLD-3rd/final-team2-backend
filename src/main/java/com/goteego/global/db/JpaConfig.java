package com.goteego.global.db;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.MongoRepository;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.goteego.user.repository",
                "com.goteego.travelPost.repository",
                "com.goteego.feed.repository",
                "com.goteego.badge.repository",
                "com.goteego.review.repository",
                "com.goteego.profileAnswer.repository",
                "com.goteego.recommendation.repository",
                "com.goteego.chat.repository" // ChatRoomRepository, UserChatRoomRepository 등 JPA Repo들
        },
        // JPA가 아닌 Repository를 제외시키는 필터
        // ㄴ MongoConfig에서 이미 MongoRepository를 지정했기 때문에 이 필터는 더욱 확실하게 역할을 분리
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoRepository.class)
)
public class JpaConfig {
}
