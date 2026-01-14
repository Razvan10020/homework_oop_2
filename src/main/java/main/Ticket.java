package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import enums.BusinessPriority;
import enums.ExpertiseArea;
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
public class Ticket {
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

    /**
     *
     * @param comment passes a comment type object to add to
     *                the list of all comment sassociated with the thicket
     */
    public void addCommentToTicket(final Comment comment) {
        this.comments.add(comment);
    }

}
