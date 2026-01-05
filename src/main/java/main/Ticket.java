package main;

import enums.BusinessPriority;
import enums.ExpertiseArea;
import enums.Status;
import fileio.ActionInput;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class Ticket {
    //luate din input
    private final String type;
    private final String title;
    private final BusinessPriority businessPriority;
    private final ExpertiseArea expertiseArea;
    private final String description;
    private final String reportedBy;
    //folosite pentru logica
    private final int id;
    private Status status;
    private String createdAt;
    private String assignedAt;
    private String solvedAt;
    private String assignedTo;

    public Ticket(final int id, final ActionInput actionInput) {
        this.type = actionInput.getParams().getType();
        this.title = actionInput.getParams().getTitle();
        this.businessPriority = actionInput.getParams().getBusinessPriority();
        this.expertiseArea = actionInput.getParams().getExpertiseArea();
        this.description = actionInput.getParams().getDescription();
        this.reportedBy = actionInput.getParams().getReportedBy();
        this.id = id;
    }
}
