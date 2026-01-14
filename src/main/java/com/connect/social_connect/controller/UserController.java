package com.connect.social_connect.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connect.social_connect.domain.User;
import com.connect.social_connect.domain.request.ReqUpdateProfileDTO;
import com.connect.social_connect.domain.request.ReqUpdateUserRoleDTO;
import com.connect.social_connect.domain.response.ResPublicProfileDTO;
import com.connect.social_connect.domain.response.ResUserDTO;
import com.connect.social_connect.domain.response.ResUserProfileDTO;
import com.connect.social_connect.domain.response.ResultPaginationDTO;
import com.connect.social_connect.service.UserService;
import com.connect.social_connect.util.SecurityUtil;
import com.connect.social_connect.util.annotation.ApiMessage;
import com.connect.social_connect.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //User Profile Endpoints

     //Get current user's full profile
    @GetMapping("/me")
    @ApiMessage("Lấy thông tin profile thành công")
    public ResponseEntity<ResUserProfileDTO> getCurrentUserProfile() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy thông tin người dùng"));

        User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("Không tìm thấy thông tin người dùng");
        }

        return ResponseEntity.ok(userService.convertToResUserProfileDTO(user));
    }


     //Update current user's profile
    @PutMapping("/me")
    @ApiMessage("Cập nhật profile thành công")
    public ResponseEntity<ResUserProfileDTO> updateCurrentUserProfile(
            @Valid @RequestBody ReqUpdateProfileDTO dto) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy thông tin người dùng"));

        User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("Không tìm thấy thông tin người dùng");
        }

        User updatedUser = userService.updateUserProfile(user.getId(), dto);
        return ResponseEntity.ok(userService.convertToResUserProfileDTO(updatedUser));
    }

     // Get public profile by user id
    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin public profile thành công")
    public ResponseEntity<ResPublicProfileDTO> getPublicProfile(@PathVariable Long id) throws IdInvalidException {
        User user = userService.fetchUserById(id);
        if (user == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(userService.convertToResPublicProfileDTO(user));
    }

    //Admin Endpoints

     //Get paginated user list with filtering (Admin)
    @GetMapping
    @ApiMessage("Lấy danh sách người dùng thành công")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(
            @Filter Specification<User> spec,
            Pageable pageable) {
        return ResponseEntity.ok(userService.fetchAllUsers(spec, pageable));
    }

     //Get user details with role (Admin)
    @GetMapping("/{id}/admin")
    @ApiMessage("Lấy thông tin chi tiết người dùng thành công")
    public ResponseEntity<ResUserDTO> getUserByIdAdmin(@PathVariable Long id) throws IdInvalidException {
        User user = userService.fetchUserById(id);
        if (user == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(userService.convertToResUserDTO(user));
    }

     //Update user's role (Admin)
    @PutMapping("/{id}/role")
    @ApiMessage("Cập nhật role người dùng thành công")
    public ResponseEntity<ResUserDTO> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateUserRoleDTO dto) throws IdInvalidException {
        User updatedUser = userService.updateUserRole(id, dto.getRoleId());
        return ResponseEntity.ok(userService.convertToResUserDTO(updatedUser));
    }

     //Delete user (Admin)
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa người dùng thành công")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws IdInvalidException {
        userService.deleteUser(id);
        return ResponseEntity.ok(null);
    }

}
