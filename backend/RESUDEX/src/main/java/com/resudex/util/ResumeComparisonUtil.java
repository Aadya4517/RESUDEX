package com.resudex.util;

import com.resudex.model.ResumeComparison;
import com.resudex.model.ResumeScore;

public class ResumeComparisonUtil {

    public static ResumeComparison compare(
            ResumeScore a,
            ResumeScore b
    ) {

        String better;

        if (a.getFinalScore() > b.getFinalScore()) {
            better = a.getFileName();
        } else if (b.getFinalScore() > a.getFinalScore()) {
            better = b.getFileName();
        } else {
            better = "Tie";
        }

        return new ResumeComparison(a, b, better);
    }
}