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
        NORMALIZATION_MAP.put("nextjs", "next.js");
        NORMALIZATION_MAP.put("tailwind css", "tailwind");
        NORMALIZATION_MAP.put("genai", "generative ai");
        NORMALIZATION_MAP.put("llms", "llm");
        NORMALIZATION_MAP.put("vector database", "vector db");
        NORMALIZATION_MAP.put("langchain", "langchain");
        NORMALIZATION_MAP.put("pinecone", "pinecone");
        NORMALIZATION_MAP.put("aws lambda", "serverless");
        NORMALIZATION_MAP.put("serverless architecture", "serverless");
        NORMALIZATION_MAP.put("azure functions", "serverless");
        NORMALIZATION_MAP.put("google cloud platform", "gcp");
    }

    public static String normalize(String skill) {
        String key = skill.toLowerCase().trim();
        return NORMALIZATION_MAP.getOrDefault(key, key);
    }
}