package com.resudex.util;

import java.util.HashMap;
import java.util.Map;

public class SkillDictionary {

    private static final Map<String, String> NORM = new HashMap<>();

    static {
        NORM.put("springboot", "spring boot");
        NORM.put("spring-boot", "spring boot");
        NORM.put("restful", "rest");
        NORM.put("nodejs", "node.js");
        NORM.put("js", "javascript");
        NORM.put("postgres", "sql");
        NORM.put("rdbms", "sql");
        NORM.put("c plus plus", "c++");
        NORM.put("cpp", "c++");
        NORM.put("py", "python");
        NORM.put("api development", "rest");
        NORM.put("nextjs", "next.js");
        NORM.put("tailwind css", "tailwind");
        NORM.put("genai", "generative ai");
        NORM.put("llms", "llm");
        NORM.put("vector database", "vector db");
        NORM.put("langchain", "langchain");
        NORM.put("pinecone", "pinecone");
        NORM.put("aws lambda", "serverless");
        NORM.put("serverless architecture", "serverless");
        NORM.put("azure functions", "serverless");
        NORM.put("google cloud platform", "gcp");
    }

    // normalize skill name
    public static String normalize(String skill) {
        String key = skill.toLowerCase().trim();
        return NORM.getOrDefault(key, key);
    }
}
