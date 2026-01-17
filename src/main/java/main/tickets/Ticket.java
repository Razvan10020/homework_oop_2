package main.tickets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import enums.BusinessPriority;
import enums.BusinessValue;
import enums.CustomerDemand;
import enums.ExpertiseArea;
import enums.Frequency;
import enums.Severity;
import enums.Status;
import fileio.ActionInput;
import lombok.Data;
import main.Commands.Comment;
import main.utils.Views;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonPropertyOrder({
        "id", "type", "title", "businessPriority", "status",
        "createdAt", "assignedAt", "solvedAt", "assignedTo",
        "reportedBy", "comments"
})
public abstract class Ticket {
    //luate din input
    @JsonView(Views.AssignedTicketView.class)
    private final String type;
    @JsonView(Views.AssignedTicketView.class)
    private final String title;
    @JsonView(Views.AssignedTicketView.class)
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
    @JsonView(Views.AssignedTicketView.class)
    private final String reportedBy;
    //folosite pentru logica
    @JsonView(Views.AssignedTicketView.class)
    private final int id;
    @JsonView(Views.AssignedTicketView.class)
    private Status status;
    @JsonView(Views.AssignedTicketView.class)
    private String createdAt;

    @JsonView(Views.AssignedTicketView.class)
    private String assignedAt = "";
    @JsonView(Views.GeneralTicketView.class)
    private String solvedAt = "";
    @JsonIgnore
    private String closedAt = "";
    @JsonView(Views.GeneralTicketView.class)
    private String assignedTo = "";
    @JsonIgnore
    private String assignedMilestone = "";
    @JsonView(Views.AssignedTicketView.class)
    private List<Comment> comments = new ArrayList<>();
    @JsonIgnore
    private List<main.utils.Action> history = new ArrayList<>();

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

    public abstract double calculateImpact();

    public abstract double calculateRisk();

    // Helper methods for subclasses
    protected int getBusinessPriorityValue(BusinessPriority priority) {
        return switch (priority) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

    protected int getFrequencyValue(Frequency frequency) {
        if (frequency == null) {
            return 0;
        }
        return switch (frequency) {
            case RARE -> 1;
            case OCCASIONAL -> 2;
            case FREQUENT -> 3;
            case ALWAYS -> 4;
        };
    }

    protected int getSeverityValue(Severity severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity) {
            case MINOR -> 1;
            case MODERATE -> 2;
            case SEVERE -> 3;
        };
    }

    protected int getBusinessValue(BusinessValue value) {
        if (value == null) {
            return 0;
        }
        return switch (value) {
            case S -> 1;
            case M -> 3;
            case L -> 6;
            case XL -> 10;
        };
    }

    protected int getCustomerDemandValue(CustomerDemand demand) {
        if (demand == null) {
            return 0;
        }
        return switch (demand) {
            case LOW -> 1;
            case MEDIUM -> 3;
            case HIGH -> 6;
            case VERY_HIGH -> 10;
        };
    }

    /**
     *
     * @param comment passes a comment type object to add to
     *                the list of all comment sassociated with the thicket
     */
    public void addCommentToTicket(final Comment comment) {
        this.comments.add(comment);
    }
}
