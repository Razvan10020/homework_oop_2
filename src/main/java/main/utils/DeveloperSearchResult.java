package main.utils;

import enums.ExpertiseArea;
import enums.Seniority;
import lombok.Getter;
import lombok.Setter;

public class DeveloperSearchResult {
    @Getter @Setter
    private String username;
    @Getter @Setter
    private ExpertiseArea expertiseArea;
    @Getter @Setter
    private Seniority seniority;
    @Getter @Setter
    private double performanceScore;
    @Getter @Setter
    private String hireDate;

    public DeveloperSearchResult(final main.Users.Developer developer) {
        this.username = developer.getUsername();
        this.expertiseArea = developer.getExpertiseArea();
        this.seniority = developer.getSeniority();
        this.performanceScore = developer.getPerformanceScore();
        this.hireDate = developer.getHireDate();
    }
}
