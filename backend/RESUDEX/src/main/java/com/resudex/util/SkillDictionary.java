package com.resudex.util;

import java.util.HashMap;
import java.util.Map;

public class SkillDictionary {

    private static final Map<String, String> NORMALIZATION_MAP = new HashMap<>();

    static {
        NORMALIZATION_MAP.put("springboot", "spring boot");
        NORMALIZATION_MAP.put("spring-boot", "spring boot");
        NORMALIZATION_MAP.put("restful", "rest");
        NORMALIZATION_MAP.put("nodejs", "node.js");
        NORMALIZATION_MAP.put("js", "javascript");
        NORMALIZATION_MAP.put("postgres", "sql");
        NORMALIZATION_MAP.put("rdbms", "sql");
        NORMALIZATION_MAP.put("c plus plus", "c++");
        NORMALIZATION_MAP.put("cpp", "c++");
        NORMALIZATION_MAP.put("py", "python");
        NORMALIZATION_MAP.put("api development", "rest");
    }

    public static String normalize(String skill) {
        String key = skill.toLowerCase().trim();
        return NORMALIZATION_MAP.getOrDefault(key, key);
    }
}