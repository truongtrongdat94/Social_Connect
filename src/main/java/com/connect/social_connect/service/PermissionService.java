package com.connect.social_connect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connect.social_connect.domain.Permission;
import com.connect.social_connect.domain.Role;
import com.connect.social_connect.domain.request.ReqCreatePermissionDTO;
import com.connect.social_connect.domain.request.ReqUpdatePermissionDTO;
import com.connect.social_connect.domain.response.ResPermissionDTO;
import com.connect.social_connect.domain.response.ResultPaginationDTO;
import com.connect.social_connect.repository.PermissionRepository;
import com.connect.social_connect.util.error.IdInvalidException;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    // Create a new permission
    public Permission createPermission(ReqCreatePermissionDTO dto) throws IdInvalidException {
        // Check if apiPath + method combination already exists
        if (permissionRepository.existsByApiPathAndMethod(dto.getApiPath(), dto.getMethod())) {
            throw new IdInvalidException(
                    "Permission với apiPath '" + dto.getApiPath() + "' và method '" + dto.getMethod() + "' đã tồn tại");
        }

        Permission permission = new Permission();
        permission.setName(dto.getName());
        permission.setApiPath(dto.getApiPath());
        permission.setMethod(dto.getMethod());
        permission.setModule(dto.getModule());

        return permissionRepository.save(permission);
    }

    // Fetch permission by ID
    public Permission fetchPermissionById(Long id) {
        return permissionRepository.findById(id).orElse(null);
    }


    // Fetch all permissions with pagination
    public ResultPaginationDTO fetchAllPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> pagePermission = permissionRepository.findAll(spec, pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pagePermission.getTotalPages());
        meta.setTotal(pagePermission.getTotalElements());

        result.setMeta(meta);

        List<ResPermissionDTO> permissionList = pagePermission.getContent()
                .stream()
                .map(this::convertToResPermissionDTO)
                .collect(Collectors.toList());
        result.setResult(permissionList);

        return result;
    }

    // Update permission
    public Permission updatePermission(Long id, ReqUpdatePermissionDTO dto) throws IdInvalidException {
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission == null) {
            throw new IdInvalidException("Permission với id = " + id + " không tồn tại");
        }

        // Check apiPath + method uniqueness if being updated
        String newApiPath = dto.getApiPath() != null ? dto.getApiPath() : permission.getApiPath();
        String newMethod = dto.getMethod() != null ? dto.getMethod() : permission.getMethod();

        // Only check uniqueness if apiPath or method is changing
        boolean isApiPathChanging = dto.getApiPath() != null && !dto.getApiPath().equals(permission.getApiPath());
        boolean isMethodChanging = dto.getMethod() != null && !dto.getMethod().equals(permission.getMethod());

        if (isApiPathChanging || isMethodChanging) {
            if (permissionRepository.existsByApiPathAndMethod(newApiPath, newMethod)) {
                throw new IdInvalidException(
                        "Permission với apiPath '" + newApiPath + "' và method '" + newMethod + "' đã tồn tại");
            }
        }

        if (dto.getName() != null) {
            permission.setName(dto.getName());
        }
        if (dto.getApiPath() != null) {
            permission.setApiPath(dto.getApiPath());
        }
        if (dto.getMethod() != null) {
            permission.setMethod(dto.getMethod());
        }
        if (dto.getModule() != null) {
            permission.setModule(dto.getModule());
        }

        return permissionRepository.save(permission);
    }

    // Delete permission with cascade removal from roles
    @Transactional
    public void deletePermission(Long id) throws IdInvalidException {
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission == null) {
            throw new IdInvalidException("Permission với id = " + id + " không tồn tại");
        }

        // Remove permission from all roles (cascade removal)
        List<Role> roles = permission.getRoles();
        if (roles != null && !roles.isEmpty()) {
            for (Role role : roles) {
                role.getPermissions().remove(permission);
            }
        }

        permissionRepository.delete(permission);
    }

    // Check if apiPath + method combination exists
    public boolean existsByApiPathAndMethod(String apiPath, String method) {
        return permissionRepository.existsByApiPathAndMethod(apiPath, method);
    }

    // DTO Converter
    public ResPermissionDTO convertToResPermissionDTO(Permission permission) {
        ResPermissionDTO dto = new ResPermissionDTO();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setApiPath(permission.getApiPath());
        dto.setMethod(permission.getMethod());
        dto.setModule(permission.getModule());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        return dto;
    }
}
