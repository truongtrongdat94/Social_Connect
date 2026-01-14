package com.connect.social_connect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.connect.social_connect.domain.Role;
import com.connect.social_connect.domain.User;
import com.connect.social_connect.domain.request.ReqCreateUserDTO;
import com.connect.social_connect.domain.request.ReqUpdateProfileDTO;
import com.connect.social_connect.domain.response.ResAccountDTO;
import com.connect.social_connect.domain.response.ResCreateUserDTO;
import com.connect.social_connect.domain.response.ResLoginDTO;
import com.connect.social_connect.domain.response.ResPublicProfileDTO;
import com.connect.social_connect.domain.response.ResUserDTO;
import com.connect.social_connect.domain.response.ResUserProfileDTO;
import com.connect.social_connect.domain.response.ResultPaginationDTO;
import com.connect.social_connect.repository.RoleRepository;
import com.connect.social_connect.repository.UserRepository;
import com.connect.social_connect.util.constant.AuthProviderEnum;
import com.connect.social_connect.util.error.IdInvalidException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

     //Fetch user by ID
    public User fetchUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

     //Update user profile
    public User updateUserProfile(Long id, ReqUpdateProfileDTO dto) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }

        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getCoverUrl() != null) {
            user.setCoverUrl(dto.getCoverUrl());
        }

        return userRepository.save(user);
    }

     //Fetch all users
    public ResultPaginationDTO fetchAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = userRepository.findAll(spec, pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        result.setMeta(meta);

        List<ResUserDTO> userList = pageUser.getContent()
                .stream()
                .map(this::convertToResUserDTO)
                .collect(Collectors.toList());
        result.setResult(userList);

        return result;
    }

    //Admin User

     //Update user role
    public User updateUserRole(Long userId, Long roleId) throws IdInvalidException {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IdInvalidException("User với id = " + userId + " không tồn tại");
        }

        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            throw new IdInvalidException("Role với id = " + roleId + " không tồn tại");
        }

        user.setRole(role);
        return userRepository.save(user);
    }

     //Delete user
    public void deleteUser(Long id) throws IdInvalidException {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }
        userRepository.delete(user);
    }

    //DTO Converters
     //Convert User to ResUserProfileDTO
    public ResUserProfileDTO convertToResUserProfileDTO(User user) {
        ResUserProfileDTO dto = new ResUserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCoverUrl(user.getCoverUrl());
        dto.setAuthProvider(user.getAuthProvider() != null ? user.getAuthProvider().name() : null);
        dto.setIsEmailVerified(user.getIsEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getRole() != null) {
            ResUserProfileDTO.RoleDTO roleDTO = new ResUserProfileDTO.RoleDTO();
            roleDTO.setId(user.getRole().getId());
            roleDTO.setName(user.getRole().getName());
            dto.setRole(roleDTO);
        }

        return dto;
    }

     //Convert User to ResPublicProfileDTO
    public ResPublicProfileDTO convertToResPublicProfileDTO(User user) {
        ResPublicProfileDTO dto = new ResPublicProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setBio(user.getBio());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCoverUrl(user.getCoverUrl());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

     //Convert User to ResUserDTO
    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO dto = new ResUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setAuthProvider(user.getAuthProvider() != null ? user.getAuthProvider().name() : null);
        dto.setIsEmailVerified(user.getIsEmailVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        if (user.getRole() != null) {
            ResUserDTO.RoleDTO roleDTO = new ResUserDTO.RoleDTO();
            roleDTO.setId(user.getRole().getId());
            roleDTO.setName(user.getRole().getName());
            dto.setRole(roleDTO);
        }

        return dto;
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

     //Check if username already exists
    public boolean isUsernameExist(String username) {
        return userRepository.existsByUsername(username);
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

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = baseUsername;
        return username;
    }
}
