package com.goteego.badge.repository;

import com.goteego.badge.domain.Badge;
import com.goteego.badge.domain.enumerate.BadgeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByCode(BadgeCode code);
}
