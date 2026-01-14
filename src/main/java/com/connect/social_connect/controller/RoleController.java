package com.connect.social_connect.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connect.social_connect.domain.Role;
import com.connect.social_connect.domain.request.ReqCreateRoleDTO;
import com.connect.social_connect.domain.request.ReqUpdateRoleDTO;
import com.connect.social_connect.domain.response.ResRoleDTO;
import com.connect.social_connect.domain.response.ResultPaginationDTO;
import com.connect.social_connect.service.RoleService;
import com.connect.social_connect.util.annotation.ApiMessage;
import com.connect.social_connect.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Create new role
    @PostMapping
    @ApiMessage("Tạo role thành công")
    public ResponseEntity<ResRoleDTO> createRole(@Valid @RequestBody ReqCreateRoleDTO dto)
            throws IdInvalidException {
        Role role = roleService.createRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.convertToResRoleDTO(role));
    }


    // Get paginated role list
    @GetMapping
    @ApiMessage("Lấy danh sách role thành công")
    public ResponseEntity<ResultPaginationDTO> getAllRoles(
            @Filter Specification<Role> spec,
            Pageable pageable) {
        return ResponseEntity.ok(roleService.fetchAllRoles(spec, pageable));
    }

    // Get role by id with permissions
    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin role thành công")
    public ResponseEntity<ResRoleDTO> getRoleById(@PathVariable Long id) throws IdInvalidException {
        Role role = roleService.fetchRoleById(id);
        if (role == null) {
            throw new IdInvalidException("Role với id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(roleService.convertToResRoleDTO(role));
    }

    // Update role
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật role thành công")
    public ResponseEntity<ResRoleDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateRoleDTO dto) throws IdInvalidException {
        Role role = roleService.updateRole(id, dto);
        return ResponseEntity.ok(roleService.convertToResRoleDTO(role));
    }

    // Delete role
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa role thành công")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) throws IdInvalidException {
        roleService.deleteRole(id);
        return ResponseEntity.ok(null);
    }

    // Bulk assign permissions to role
    @PutMapping("/{id}/permissions")
    @ApiMessage("Gán permissions cho role thành công")
    public ResponseEntity<ResRoleDTO> assignPermissions(
            @PathVariable Long id,
            @RequestBody List<Long> permissionIds) throws IdInvalidException {
        Role role = roleService.assignPermissions(id, permissionIds);
        return ResponseEntity.ok(roleService.convertToResRoleDTO(role));
    }
}
