package com.connect.social_connect.service;

import org.springframework.stereotype.Service;

import com.connect.social_connect.domain.Role;
import com.connect.social_connect.domain.User;
import com.connect.social_connect.domain.response.ResAccountDTO;
import com.connect.social_connect.domain.response.ResCreateUserDTO;
import com.connect.social_connect.domain.response.ResLoginDTO;
import com.connect.social_connect.repository.RoleRepository;
import com.connect.social_connect.repository.UserRepository;
import com.connect.social_connect.util.constant.AuthProviderEnum;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


     //Create a new user with default role
    public User handleCreateUser(User user) {
        // Assign default USER role if not set
        if (user.getRole() == null) {
            Role userRole = roleRepository.findByName("USER");
            user.setRole(userRole);
        }
        return userRepository.save(user);
    }

     //Get user by email (username)
    public User handleGetUserByUsername(String email) {
        return userRepository.findByEmail(email);
    }

     //Update user refresh token
    public void updateUserToken(String token, String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setRefreshToken(token);
            userRepository.save(user);
        }
    }

     //Get user by refresh token
    public User getUserByRefreshToken(String token) {
        return userRepository.findByRefreshToken(token);
    }

     //Check if email already exists
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

     //Convert User entity to ResCreateUserDTO
    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO dto = new ResCreateUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setIsEmailVerified(user.getIsEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

     //Convert User entity to ResAccountDTO
    public ResAccountDTO convertToResAccountDTO(User user) {
        ResAccountDTO dto = new ResAccountDTO();
        ResAccountDTO.UserAccount userAccount = new ResAccountDTO.UserAccount();

        userAccount.setId(user.getId());
        userAccount.setEmail(user.getEmail());
        userAccount.setUsername(user.getUsername());
        userAccount.setDisplayName(user.getDisplayName());
        userAccount.setBio(user.getBio());
        userAccount.setAvatarUrl(user.getAvatarUrl());

        if (user.getRole() != null) {
            ResLoginDTO.RoleDTO roleDTO = new ResLoginDTO.RoleDTO();
            roleDTO.setId(user.getRole().getId());
            roleDTO.setName(user.getRole().getName());
            userAccount.setRole(roleDTO);
        }

        dto.setUser(userAccount);
        return dto;
    }

     //Convert User entity to ResLoginDTO.UserLogin
    public ResLoginDTO.UserLogin convertToUserLogin(User user) {
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(user.getId());
        userLogin.setEmail(user.getEmail());
        userLogin.setUsername(user.getUsername());
        userLogin.setDisplayName(user.getDisplayName());

        if (user.getRole() != null) {
            ResLoginDTO.RoleDTO roleDTO = new ResLoginDTO.RoleDTO();
            roleDTO.setId(user.getRole().getId());
            roleDTO.setName(user.getRole().getName());
            userLogin.setRole(roleDTO);
        }

        return userLogin;
    }

     //Activate user by setting isEmailVerified to true
    public User activateUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setIsEmailVerified(true);
            return userRepository.save(user);
        }
        return null;
    }

     //Find existing user by email or create new user with Google OAuth2 data.
    public User findOrCreateGoogleUser(String email, String name, String picture) {
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            // Update avatar if user doesn't have one
            if (existingUser.getAvatarUrl() == null && picture != null) {
                existingUser.setAvatarUrl(picture);
                return userRepository.save(existingUser);
            }
            return existingUser;
        }

        // Create new user with Google data
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(generateUniqueUsername(email));
        newUser.setDisplayName(name != null ? name : email.split("@")[0]);
        newUser.setAvatarUrl(picture);
        newUser.setAuthProvider(AuthProviderEnum.GOOGLE);
        newUser.setIsEmailVerified(true); // Google users are automatically verified

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER");
        newUser.setRole(userRole);

        return userRepository.save(newUser);
    }

    /**
     * Generate unique username from email.
     * If base username exists, append number suffix.
     */
    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }
}
