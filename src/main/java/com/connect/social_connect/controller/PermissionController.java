package com.connect.social_connect.controller;

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

import com.connect.social_connect.domain.Permission;
import com.connect.social_connect.domain.request.ReqCreatePermissionDTO;
import com.connect.social_connect.domain.request.ReqUpdatePermissionDTO;
import com.connect.social_connect.domain.response.ResPermissionDTO;
import com.connect.social_connect.domain.response.ResultPaginationDTO;
import com.connect.social_connect.service.PermissionService;
import com.connect.social_connect.util.annotation.ApiMessage;
import com.connect.social_connect.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    // Create new permission
    @PostMapping
    @ApiMessage("Tạo permission thành công")
    public ResponseEntity<ResPermissionDTO> createPermission(@Valid @RequestBody ReqCreatePermissionDTO dto)
            throws IdInvalidException {
        Permission permission = permissionService.createPermission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.convertToResPermissionDTO(permission));
    }


    // Get paginated permission list with module filter
    @GetMapping
    @ApiMessage("Lấy danh sách permission thành công")
    public ResponseEntity<ResultPaginationDTO> getAllPermissions(
            @Filter Specification<Permission> spec,
            Pageable pageable) {
        return ResponseEntity.ok(permissionService.fetchAllPermissions(spec, pageable));
    }

    // Get permission by id
    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin permission thành công")
    public ResponseEntity<ResPermissionDTO> getPermissionById(@PathVariable Long id) throws IdInvalidException {
        Permission permission = permissionService.fetchPermissionById(id);
        if (permission == null) {
            throw new IdInvalidException("Permission với id = " + id + " không tồn tại");
        }
        return ResponseEntity.ok(permissionService.convertToResPermissionDTO(permission));
    }

    // Update permission
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật permission thành công")
    public ResponseEntity<ResPermissionDTO> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdatePermissionDTO dto) throws IdInvalidException {
        Permission permission = permissionService.updatePermission(id, dto);
        return ResponseEntity.ok(permissionService.convertToResPermissionDTO(permission));
    }

    // Delete permission
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa permission thành công")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) throws IdInvalidException {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(null);
    }
}
