package com.resudex.util;

import com.resudex.model.RoleDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoleFitCalculator {

    public static Map<String, Integer> calculateRoleFit(
            Set<String> resumeSkills
    ) {

        Map<String, Integer> roleFit = new HashMap<>();

        for (RoleDefinition role : RoleRepository.getRoles()) {
            long matched = role.getRequiredSkills()
                    .stream()
                    .filter(resumeSkills::contains)
                    .count();

            int fitPercent = (int) ((matched * 100.0) / role.getRequiredSkills().size());
            roleFit.put(role.getRoleName(), fitPercent);
        }

        return roleFit;
    }
}