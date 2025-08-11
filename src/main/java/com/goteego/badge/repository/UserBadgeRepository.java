package com.goteego.badge.repository;

import com.goteego.badge.domain.Badge;
import com.goteego.badge.domain.UserBadge;
import com.goteego.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(Long userId);

    boolean existsByUserAndBadge(User user, Badge badge);

    void deleteByUserAndBadge(User user, Badge badge);
    Optional<UserBadge> findByUser_IdAndBadge_Id(Long userId, Long badgeId);
}
