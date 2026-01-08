package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import enums.BusinessPriority;
import enums.ExpertiseArea;
import enums.Status;
import fileio.ActionInput;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonPropertyOrder({
        "id", "type", "title", "businessPriority", "status",
        "createdAt", "assignedAt", "solvedAt", "assignedTo",
        "reportedBy", "comments"
})
public class Ticket {
    //luate din input
    private final String type;
    private final String title;
    private BusinessPriority businessPriority;

    //update tickets variables
    @JsonIgnore
    private int changeDaysAgo = 0;
    @JsonIgnore
    private String lastUpdatedDay;
    @JsonIgnore
    private String blockedDate;

    @JsonIgnore
    private final ExpertiseArea expertiseArea;
    @JsonIgnore
    private final String description;
    private final String reportedBy;
    //folosite pentru logica
    private final int id;
    private Status status;
    private String createdAt;
    private String assignedAt = "";
    private String solvedAt = "";
    private String assignedTo = "";
    @JsonIgnore
    private String assignedMilestone = "";
    private List<String> comments = new ArrayList<>();

    public Ticket(final int id, final ActionInput actionInput) {
        this.type = actionInput.asParams().getType();
        this.title = actionInput.asParams().getTitle();
        this.businessPriority = actionInput.asParams().getBusinessPriority();
        this.expertiseArea = actionInput.asParams().getExpertiseArea();
        this.description = actionInput.asParams().getDescription();
        this.reportedBy = actionInput.asParams().getReportedBy();
        this.id = id;

        this.createdAt = actionInput.getTimestamp();
        this.status = Status.OPEN;
    }

    public void assignTo(final String assignedTo) {
        this.assignedTo = assignedTo;
    }

}
