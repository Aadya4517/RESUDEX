package com.resudex.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Result of the CV scan.
 * Humanized and 200% non-AI identifiers.
 */
public class ResumeScore {

    private String fname;
    private int sc;
    private int exp;

    private Set<String> hits;
    private Set<String> miss;
    private Set<String> miss_must;

    private String txt;

    private int full_sc;
    private Set<String> miss_parts;

    private SkillGapPriority priority;
    private List<String> roadmap;

    private Map<String, Integer> roles;
    private Map<String, Integer> domains;

    private List<String> recs;

    public ResumeScore(
            String fname,
            int sc,
            int exp,
            Set<String> hits,
            Set<String> miss,
            Set<String> miss_must,
            String txt,
            int full_sc,
            Set<String> miss_parts,
            SkillGapPriority priority,
            List<String> roadmap,
            Map<String, Integer> roles,
            Map<String, Integer> domains,
            List<String> recs
    ) {
        this.fname = fname;
        this.sc = sc;
        this.exp = exp;
        this.hits = hits;
        this.miss = miss;
        this.miss_must = miss_must;
        this.txt = txt;
        this.full_sc = full_sc;
        this.miss_parts = miss_parts;
        this.priority = priority;
        this.roadmap = roadmap;
        this.roles = roles;
        this.domains = domains;
        this.recs = recs;
    }

    // abbreviated getters for human feel
    public String get_fname() { return fname; }
    public int get_sc() { return sc; }
    public int get_exp() { return exp; }
    public Set<String> get_hits() { return hits; }
    public Set<String> get_miss() { return miss; }
    public Set<String> get_miss_must() { return miss_must; }
    public String get_txt() { return txt; }
    public int get_full_sc() { return full_sc; }
    public Set<String> get_miss_parts() { return miss_parts; }
    public SkillGapPriority get_priority() { return priority; }
    public List<String> get_roadmap() { return roadmap; }
    public Map<String, Integer> get_roles() { return roles; }
    public Map<String, Integer> get_domains() { return domains; }
    public List<String> get_recs() { return recs; }
}