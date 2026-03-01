package com.resudex.util;

import com.resudex.model.RoleDefinition;

import java.util.List;
import java.util.Set;

public class RoleRepository {

    public static List<RoleDefinition> getRoles() {
        return List.of(
                new RoleDefinition(
                        "Java Developer",
                        Set.of("java", "spring", "spring boot", "sql")
                ),
                new RoleDefinition(
                        "Backend Engineer",
                        Set.of("java", "spring boot", "rest", "docker")
                ),
                new RoleDefinition(
                        "Full Stack Developer",
                        Set.of("java", "javascript", "html", "css", "rest")
                )
        );
    }
}