package com.resudex.model;

import java.util.Set;

public class RoleDefinition {
    private String roleName;
    private Set<String> requiredSkills;

    public RoleDefinition(String roleName, Set<String> requiredSkills) {
        this.roleName = roleName;
        this.requiredSkills = requiredSkills;
    }

    public String getRoleName() {
        return roleName;
    }

    public Set<String> getRequiredSkills() {
        return requiredSkills;
    }
}