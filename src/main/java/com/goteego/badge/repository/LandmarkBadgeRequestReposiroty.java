package com.goteego.badge.repository;


import com.goteego.badge.domain.LandmarkBadgeRequest;
import com.goteego.badge.domain.enumerate.BadgeStatus;
import com.goteego.feed.domain.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LandmarkBadgeRequestReposiroty extends JpaRepository<LandmarkBadgeRequest, Long> {
    Optional<LandmarkBadgeRequest> findByFeed(Feed feed);

    List<LandmarkBadgeRequest> findAllByStatus(BadgeStatus status);
}
