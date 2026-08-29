package com.example.riskmanagementsystem.service;

import com.example.riskmanagementsystem.model.User;
import com.example.riskmanagementsystem.model.UserProfile;
import com.example.riskmanagementsystem.repo.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserProfileRepository profileRepo;

    public UserProfileService(UserProfileRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    public UserProfile getProfileByUserId(Long userId) {
        return profileRepo.findByUser_UserId(userId).orElse(null);
    }

    public boolean userHasProfile(Long userId) {
        return profileRepo.existsByUser_UserId(userId);
    }

    public UserProfile saveOrUpdateProfile(User user,
                                           String fullName,
                                           String jobRole,
                                           String department,
                                           String phoneNumber) {

        if (fullName == null
                || fullName.trim().length() < 2) {
            throw new IllegalArgumentException(
                    "Full name must be at least 2 characters.");
        }
        if (!fullName.trim().matches("^[a-zA-Z\\s\\-']+$")) {
            throw new IllegalArgumentException(
                    "Full name can only contain letters, spaces, "
                            + "hyphens and apostrophes.");
        }
        if (jobRole == null || jobRole.trim().length() < 2) {
            throw new IllegalArgumentException(
                    "Job role must be at least 2 characters.");
        }
        if (department == null
                || department.trim().length() < 2) {
            throw new IllegalArgumentException(
                    "Department must be at least 2 characters.");
        }
        if (phoneNumber != null
                && !phoneNumber.trim().isBlank()
                && !phoneNumber.trim().matches(
                "^[\\+\\d\\s\\-\\(\\)]{7,20}$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid phone number.");
        }

        UserProfile profile = profileRepo
                .findByUser_UserId(user.getUserId())
                .orElse(new UserProfile());

        profile.setUser(user);
        profile.setFullName(fullName.trim());
        profile.setJobRole(jobRole.trim());
        profile.setDepartment(department.trim());
        profile.setPhoneNumber(
                phoneNumber != null
                        && !phoneNumber.trim().isBlank()
                        ? phoneNumber.trim() : null);

        return profileRepo.save(profile);
    }
}