package com.example.riskmanagementsystem.repo;

import com.example.riskmanagementsystem.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repository for UserProfile entity (handles DB operations)
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // find a user profile using the user ID (linked through User entity)
    Optional<UserProfile> findByUser_UserId(Long userId);

    // check if a profile already exists for a specific user
    boolean existsByUser_UserId(Long userId);
    List<UserProfile> findByUser_UserIdIn(List<Long> userIds);
}