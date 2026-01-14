package com.connect.social_connect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.connect.social_connect.domain.Permission;
import com.connect.social_connect.domain.Role;
import com.connect.social_connect.domain.request.ReqCreateRoleDTO;
import com.connect.social_connect.domain.request.ReqUpdateRoleDTO;
import com.connect.social_connect.domain.response.ResPermissionDTO;
import com.connect.social_connect.domain.response.ResRoleDTO;
import com.connect.social_connect.domain.response.ResultPaginationDTO;
import com.connect.social_connect.repository.PermissionRepository;
import com.connect.social_connect.repository.RoleRepository;
import com.connect.social_connect.util.error.IdInvalidException;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    // Create a new role
    public Role createRole(ReqCreateRoleDTO dto) throws IdInvalidException {
        // Check if role name already exists
        if (roleRepository.existsByName(dto.getName())) {
            throw new IdInvalidException("Role với tên '" + dto.getName() + "' đã tồn tại");
        }

        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setActive(dto.isActive());

        // Assign permissions if provided
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findByIdIn(dto.getPermissionIds());
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }


    // Fetch role by ID
    public Role fetchRoleById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    // Fetch all roles with pagination
    public ResultPaginationDTO fetchAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pageRole = roleRepository.findAll(spec, pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageRole.getTotalPages());
        meta.setTotal(pageRole.getTotalElements());

        result.setMeta(meta);

        List<ResRoleDTO> roleList = pageRole.getContent()
                .stream()
                .map(this::convertToResRoleDTO)
                .collect(Collectors.toList());
        result.setResult(roleList);

        return result;
    }

    // Update role
    public Role updateRole(Long id, ReqUpdateRoleDTO dto) throws IdInvalidException {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            throw new IdInvalidException("Role với id = " + id + " không tồn tại");
        }

        // Check name uniqueness if name is being updated
        if (dto.getName() != null && !dto.getName().equals(role.getName())) {
            if (roleRepository.existsByName(dto.getName())) {
                throw new IdInvalidException("Role với tên '" + dto.getName() + "' đã tồn tại");
            }
            role.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }

        if (dto.getActive() != null) {
            role.setActive(dto.getActive());
        }

        // Update permissions if provided
        if (dto.getPermissionIds() != null) {
            List<Permission> permissions = permissionRepository.findByIdIn(dto.getPermissionIds());
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }


    // Delete role (with check for assigned users)
    public void deleteRole(Long id) throws IdInvalidException {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            throw new IdInvalidException("Role với id = " + id + " không tồn tại");
        }

        // Check if role has assigned users
        long userCount = roleRepository.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new IdInvalidException("Không thể xóa role vì có " + userCount + " user đang được gán role này");
        }

        roleRepository.delete(role);
    }

    // Assign permissions to role (bulk assignment)
    public Role assignPermissions(Long roleId, List<Long> permissionIds) throws IdInvalidException {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            throw new IdInvalidException("Role với id = " + roleId + " không tồn tại");
        }

        List<Permission> permissions = permissionRepository.findByIdIn(permissionIds);
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    // Check if role name exists
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    // DTO Converters
    public ResRoleDTO convertToResRoleDTO(Role role) {
        ResRoleDTO dto = new ResRoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setActive(role.isActive());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());

        if (role.getPermissions() != null) {
            List<ResPermissionDTO> permissionDTOs = role.getPermissions()
                    .stream()
                    .map(this::convertToResPermissionDTO)
                    .collect(Collectors.toList());
            dto.setPermissions(permissionDTOs);
        }

        return dto;
    }

    private ResPermissionDTO convertToResPermissionDTO(Permission permission) {
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
