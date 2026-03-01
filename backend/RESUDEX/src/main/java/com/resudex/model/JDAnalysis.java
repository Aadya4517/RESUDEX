package com.resudex.model;

import java.util.Set;

public class JDAnalysis {

    private int totalSkills;
    private Set<String> mandatorySkills;
    private Set<String> optionalSkills;
    private int experienceRequired;
    private Set<String> softSkills;

    public JDAnalysis(
            int totalSkills,
            Set<String> mandatorySkills,
            Set<String> optionalSkills,
            int experienceRequired,
            Set<String> softSkills
    ) {
        this.totalSkills = totalSkills;
        this.mandatorySkills = mandatorySkills;
        this.optionalSkills = optionalSkills;
        this.experienceRequired = experienceRequired;
        this.softSkills = softSkills;
    }

    public int getTotalSkills() {
        return totalSkills;
    }

    public Set<String> getMandatorySkills() {
        return mandatorySkills;
    }

    public Set<String> getOptionalSkills() {
        return optionalSkills;
    }

    public int getExperienceRequired() {
        return experienceRequired;
    }

    public Set<String> getSoftSkills() {
        return softSkills;
    }
}