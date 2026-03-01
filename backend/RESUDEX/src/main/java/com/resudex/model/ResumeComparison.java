package com.resudex.model;

public class ResumeComparison {

    private ResumeScore resumeA;
    private ResumeScore resumeB;
    private String betterCandidate;

    public ResumeComparison(
            ResumeScore resumeA,
            ResumeScore resumeB,
            String betterCandidate
    ) {
        this.resumeA = resumeA;
        this.resumeB = resumeB;
        this.betterCandidate = betterCandidate;
    }

    public ResumeScore getResumeA() {
        return resumeA;
    }

    public ResumeScore getResumeB() {
        return resumeB;
    }

    public String getBetterCandidate() {
        return betterCandidate;
    }
}