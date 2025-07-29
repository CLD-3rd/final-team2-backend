package com.goteego.badge.service;

import com.goteego.badge.domain.Badge;
import com.goteego.badge.domain.LandmarkBadgeRequest;
import com.goteego.badge.domain.UserBadge;
import com.goteego.badge.domain.enumerate.BadgeCategory;
import com.goteego.badge.domain.enumerate.BadgeCode;
import com.goteego.badge.domain.enumerate.BadgeStatus;
import com.goteego.badge.dto.BadgeRequestsResponse;
import com.goteego.badge.dto.BadgeResponse;
import com.goteego.badge.repository.BadgeRepository;
import com.goteego.badge.repository.LandmarkBadgeRequestReposiroty;
import com.goteego.badge.repository.UserBadgeRepository;
import com.goteego.feed.domain.Feed;
import com.goteego.feed.repository.FeedRepository;
import com.goteego.global.domain.enumerate.Location;
import com.goteego.global.error.exception.ErrorCode;
import com.goteego.global.error.exception.NotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final FeedRepository feedRepository;

    private final LandmarkBadgeRequestReposiroty landmarkBadgeRequestReposiroty;

    public BadgeService(UserBadgeRepository userBadgeRepository, BadgeRepository badgeRepository, FeedRepository feedRepository,

                        LandmarkBadgeRequestReposiroty landmarkBadgeRequestReposiroty) {
        this.userBadgeRepository = userBadgeRepository;
        this.badgeRepository = badgeRepository;
        this.feedRepository = feedRepository;

        this.landmarkBadgeRequestReposiroty = landmarkBadgeRequestReposiroty;
    }


    public List<BadgeResponse> getBadgesByUserId(Long userId) {
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        if (userBadges.isEmpty()) {
            throw new NotFoundException(ErrorCode.NOT_FOUND_BADGE);
        }

        return userBadges.stream().map(userBadge -> BadgeResponse.from(userBadge.getBadge())).toList();
    }


    /**
     * 서비스가 제공하는 뱃지 데이터 생성
     */
    @PostConstruct
    public void initBadgeData() {
        // 없으면 생성
        if (badgeRepository.count() == 0) {
            createBadgeData();
        }
    }

    public void createBadgeData() {
        List<BadgeCode> badgeCodes = List.of(
                BadgeCode.LANDMARK_EIFFEL,
                BadgeCode.LANDMARK_HALLA,
                BadgeCode.LANDMARK_HAESHA,
                BadgeCode.LANDMARK_GWANGHWAMUN,
                BadgeCode.LANDMARK_ANMOKCAFE,
                BadgeCode.LANDMARK_EXPOBRIDGE,
                BadgeCode.LANDMARK_U_SQUARE,
                BadgeCode.LANDMARK_BULGUKSA,
                BadgeCode.LANDMARK_SEORAKSAN,
                BadgeCode.LANDMARK_JEONJUHANOK);

        for (BadgeCode code : badgeCodes) {
            Badge badge = Badge.builder().category(BadgeCategory.LANDMARK).code(code).imgUrl("https://example.com/icon.png").build();
            badgeRepository.save(badge);
        }
    }


    @Transactional
    public void processBadgeByApproval(Long feedId, boolean approved) {

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));


        if (feed.getLocation() == null) return;

        Badge badge;
        try {
            badge = getBadgeByFeed(feed.getId(), true);
        } catch (IllegalArgumentException e) {
            if (approved) throw e;
            else return;
        }

        boolean hasBadge = userBadgeRepository.existsByUserAndBadge(feed.getAuthor(), badge);

        if (approved) {
            if (!hasBadge) {
                UserBadge userBadge = UserBadge.builder().user(feed.getAuthor()).badge(badge).build();
                userBadgeRepository.save(userBadge);
            }
            LandmarkBadgeRequest request = landmarkBadgeRequestReposiroty.findByFeed(feed).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_BADGE_REQUEST));
            request.approve();


        } else {
            if (hasBadge) {
                userBadgeRepository.deleteByUserAndBadge(feed.getAuthor(), badge);
            }
            landmarkBadgeRequestReposiroty.findByFeed(feed).ifPresent(LandmarkBadgeRequest::reject);
        }
    }


    private Badge getBadgeByFeed(Long feedId, boolean useDefaultIfInvalid) {

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new NotFoundException(ErrorCode.FEED_NOT_FOUND));

        Location location = feed.getLocation();

        BadgeCode badgeCode = location.toBadgeCode(); // 안전한 변환


        if (badgeCode == null && useDefaultIfInvalid) {
            badgeCode = BadgeCode.LANDMARK_EIFFEL;
        }
        //뱃지 초기데이터 없으면 걸림
        return badgeRepository.findByCode(badgeCode).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_BADGE_CODE));
    }


    @Transactional
    public List<BadgeRequestsResponse> getBadgesRequests() {
        List<LandmarkBadgeRequest> requests = landmarkBadgeRequestReposiroty.findAllByStatus(BadgeStatus.PENDING);

        return requests.stream().map(req -> BadgeRequestsResponse.of(req.getId(), req.getFeed().getAuthor().getId(), req.getFeed().getId(), req.getStatus().name())).collect(Collectors.toList());
    }

}
