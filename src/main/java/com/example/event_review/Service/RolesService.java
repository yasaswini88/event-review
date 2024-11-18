package com.example.event_review.Service;

import com.example.event_review.Entity.Roles;
import com.example.event_review.Repo.RolesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolesService {
    @Autowired
    private RolesRepo rolesRepo;

    public List<Roles> getAllRoles() {
        return rolesRepo.findAll();
    }

    public Optional<Roles> getRoleById(Long roleId) {
        return rolesRepo.findById(roleId);
    }

    public Optional<Roles> getRoleByName(String roleName) {
        return rolesRepo.findByRoleName(roleName);
    }

    public Roles addRole(Roles role) {
        return rolesRepo.save(role);
    }

    public Roles updateRole(Long roleId, Roles updatedRole) {
        return rolesRepo.findById(roleId)
                .map(role -> {
                    role.setRoles(updatedRole.getRoleName());
                    return rolesRepo.save(role);
                })
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
    }

    public void deleteRole(Long roleId) {
        rolesRepo.deleteById(roleId);
    }
}
