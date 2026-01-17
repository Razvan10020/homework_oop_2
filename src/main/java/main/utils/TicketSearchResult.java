package main.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import enums.BusinessPriority;
import enums.Status;
import lombok.Data;
import main.tickets.Ticket;

import java.util.List;

@Data
@JsonPropertyOrder({
        "id", "type", "title", "businessPriority", "status",
        "createdAt", "solvedAt", "reportedBy", "matchingWords"
})
public class TicketSearchResult {
    private final int id;
    private final String type;
    private final String title;
    private final BusinessPriority businessPriority;
    private final Status status;
    private final String createdAt;
    private final String solvedAt;
    private final String reportedBy;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<String> matchingWords;

    public TicketSearchResult(final Ticket ticket, final List<String> matchingWords) {
        this.id = ticket.getId();
        this.type = ticket.getType();
        this.title = ticket.getTitle();
        this.businessPriority = ticket.getBusinessPriority();
        this.status = ticket.getStatus();
        this.createdAt = ticket.getCreatedAt();
        this.solvedAt = ticket.getSolvedAt();
        this.reportedBy = ticket.getReportedBy();
        this.matchingWords = matchingWords;
    }
}
