package com.resudex.model;

import java.util.List;

public class AnalysisResponse {

    private int totalResumes;
    private double averageScore;
    private int highestScore;
    private List<ResumeScore> rankedResumes;

    public AnalysisResponse(
            int totalResumes,
            double averageScore,
            int highestScore,
            List<ResumeScore> rankedResumes
    ) {
        this.totalResumes = totalResumes;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.rankedResumes = rankedResumes;
    }

    public int getTotalResumes() { return totalResumes; }
    public double getAverageScore() { return averageScore; }
    public int getHighestScore() { return highestScore; }
    public List<ResumeScore> getRankedResumes() { return rankedResumes; }
}