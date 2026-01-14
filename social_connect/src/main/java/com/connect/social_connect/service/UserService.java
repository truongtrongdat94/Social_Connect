package com.connect.social_connect.service;

import org.springframework.stereotype.Service;

import com.connect.social_connect.domain.Role;
import com.connect.social_connect.domain.User;
import com.connect.social_connect.domain.response.ResAccountDTO;
import com.connect.social_connect.domain.response.ResCreateUserDTO;
import com.connect.social_connect.domain.response.ResLoginDTO;
import com.connect.social_connect.repository.RoleRepository;
import com.connect.social_connect.repository.UserRepository;

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
}
