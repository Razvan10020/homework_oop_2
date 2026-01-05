package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.BusinessPriority;
import enums.ExpertiseArea;
import enums.Status;
import fileio.TicketInput.ActionInput;
import lombok.Data;
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
    private List<String> comments = new ArrayList<>();

    public Ticket(final int id, final ActionInput actionInput) {
        this.type = actionInput.getParams().getType();
        this.title = actionInput.getParams().getTitle();
        this.businessPriority = actionInput.getParams().getBusinessPriority();
        this.expertiseArea = actionInput.getParams().getExpertiseArea();
        this.description = actionInput.getParams().getDescription();
        this.reportedBy = actionInput.getParams().getReportedBy();
        this.id = id;

        this.createdAt = actionInput.getTimestamp();
        this.status = Status.OPEN;
    }

}
