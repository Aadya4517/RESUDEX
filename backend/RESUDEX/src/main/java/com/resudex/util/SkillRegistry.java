package com.resudex.util;

import com.resudex.model.SkillCategory;

import java.util.HashMap;
import java.util.Map;

public class SkillRegistry {

    public static final Map<String, SkillCategory> SKILLS = new HashMap<>();

    static {
        // Programming Languages
        SKILLS.put("java", SkillCategory.PROGRAMMING);
        SKILLS.put("python", SkillCategory.PROGRAMMING);
        SKILLS.put("c", SkillCategory.PROGRAMMING);
        SKILLS.put("c++", SkillCategory.PROGRAMMING);
        SKILLS.put("sql", SkillCategory.PROGRAMMING);

        // Web Development
        SKILLS.put("html", SkillCategory.WEB);
        SKILLS.put("css", SkillCategory.WEB);
        SKILLS.put("javascript", SkillCategory.WEB);
        SKILLS.put("react", SkillCategory.WEB);
        SKILLS.put("angular", SkillCategory.WEB);
        SKILLS.put("node.js", SkillCategory.WEB);

        // Backend
        SKILLS.put("spring", SkillCategory.BACKEND);
        SKILLS.put("spring boot", SkillCategory.BACKEND);
        SKILLS.put("rest", SkillCategory.BACKEND);
        SKILLS.put("microservices", SkillCategory.BACKEND);

        // Data / Python Stack
        SKILLS.put("pandas", SkillCategory.DATA);
        SKILLS.put("numpy", SkillCategory.DATA);
        SKILLS.put("data analysis", SkillCategory.DATA);

        // DevOps
        SKILLS.put("docker", SkillCategory.DEVOPS);
        SKILLS.put("git", SkillCategory.DEVOPS);
        SKILLS.put("linux", SkillCategory.DEVOPS);
    }

    public static SkillCategory getCategory(String skill) {
        return SKILLS.get(skill);
    }
}