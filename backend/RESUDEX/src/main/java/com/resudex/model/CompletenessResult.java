package com.resudex.model;

import java.util.Set;

public class CompletenessResult {

    private int completenessScore;
    private Set<String> missingSections;

    public CompletenessResult(int completenessScore, Set<String> missingSections) {
        this.completenessScore = completenessScore;
        this.missingSections = missingSections;
    }

    public int getCompletenessScore() {
        return completenessScore;
    }

    public Set<String> getMissingSections() {
        return missingSections;
    }
}