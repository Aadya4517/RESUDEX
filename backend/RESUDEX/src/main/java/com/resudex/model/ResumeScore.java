package com.resudex.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

// result of CV scan
public class ResumeScore {

    private String filename;
    private int sc;
    private int exp;

    private Set<String> hits;
    private Set<String> miss;
    private Set<String> missMust;

    private String txt;

    private int fullSc;
    private Set<String> missParts;

    private SkillGapPriority priority;
    private List<String> roadmap;

    private Map<String, Integer> roles;
    private Map<String, Integer> domains;

    private List<String> recs;

    public ResumeScore(
            String filename,
            int sc,
            int exp,
            Set<String> hits,
            Set<String> miss,
            Set<String> missMust,
            String txt,
            int fullSc,
            Set<String> missParts,
            SkillGapPriority priority,
            List<String> roadmap,
            Map<String, Integer> roles,
            Map<String, Integer> domains,
            List<String> recs
    ) {
        this.filename = filename;
        this.sc = sc;
        this.exp = exp;
        this.hits = hits;
        this.miss = miss;
        this.missMust = missMust;
        this.txt = txt;
        this.fullSc = fullSc;
        this.missParts = missParts;
        this.priority = priority;
        this.roadmap = roadmap;
        this.roles = roles;
        this.domains = domains;
        this.recs = recs;
    }

    // getters
    public String get_fname() { return filename; }
    public int get_sc() { return sc; }
    public int get_exp() { return exp; }
    public Set<String> get_hits() { return hits; }
    public Set<String> get_miss() { return miss; }
    public Set<String> get_miss_must() { return missMust; }
    public String get_txt() { return txt; }
    public int get_full_sc() { return fullSc; }
    public Set<String> get_miss_parts() { return missParts; }
    public SkillGapPriority get_priority() { return priority; }
    public List<String> get_roadmap() { return roadmap; }
    public Map<String, Integer> get_roles() { return roles; }
    public Map<String, Integer> get_domains() { return domains; }
    public List<String> get_recs() { return recs; }
}
