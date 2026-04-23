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
        SKILLS.put("next.js", SkillCategory.WEB);
        SKILLS.put("tailwind", SkillCategory.WEB);

        // Backend
        SKILLS.put("spring", SkillCategory.BACKEND);
        SKILLS.put("spring boot", SkillCategory.BACKEND);
        SKILLS.put("rest", SkillCategory.BACKEND);
        SKILLS.put("microservices", SkillCategory.BACKEND);
        SKILLS.put("node", SkillCategory.BACKEND);

        // Data / Python Stack / AI
        SKILLS.put("pandas", SkillCategory.DATA);
        SKILLS.put("numpy", SkillCategory.DATA);
        SKILLS.put("data analysis", SkillCategory.DATA);
        SKILLS.put("generative ai", SkillCategory.DATA);
        SKILLS.put("llm", SkillCategory.DATA);
        SKILLS.put("langchain", SkillCategory.DATA);
        SKILLS.put("pinecone", SkillCategory.DATA);
        SKILLS.put("vector db", SkillCategory.DATA);

        // DevOps / Cloud
        SKILLS.put("docker", SkillCategory.DEVOPS);
        SKILLS.put("git", SkillCategory.DEVOPS);
        SKILLS.put("linux", SkillCategory.DEVOPS);
        SKILLS.put("aws", SkillCategory.DEVOPS);
        SKILLS.put("gcp", SkillCategory.DEVOPS);
        SKILLS.put("azure", SkillCategory.DEVOPS);
        SKILLS.put("terraform", SkillCategory.DEVOPS);
        SKILLS.put("serverless", SkillCategory.DEVOPS);
    }

    public static SkillCategory getCategory(String skill) {
        return SKILLS.get(skill);
    }
}