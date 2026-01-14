package main.Commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import enums.Role;
import enums.Status;
import fileio.ActionInput;
import lombok.Data;
import main.UserManger;
import main.Users.DeveloperRepartition;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonPropertyOrder({
        "name", "blockingFor", "dueDate", "createdAt", "tickets",
        "assignedDevs", "createdBy", "status", "isBlocked",
        "daysUntilDue", "overdueBy", "openTickets", "closedTickets",
        "completionPercentage", "repartition"
})
public class Milestone {
    //entry data
    private String name;
    private String[] blockingFor;
    private String dueDate;
    private int[] tickets;
    private String[] assignedDevs;

    //created data
    private String createdAt;
    private String createdBy;
    private Status status;
    @JsonProperty("isBlocked")
    private boolean blocked;
    private int daysUntilDue;
    private int overdueBy;
    private List<Integer> openTickets;
    private List<Integer> closedTickets;
    private double completionPercentage;
    private List<DeveloperRepartition> repartition;

    @JsonIgnore
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Milestone(ActionInput actionInput, UserManger userManger) {
        this.name = actionInput.asMilestone().getName();
        this.blockingFor = actionInput.asMilestone().getBlockingFor();
        this.dueDate = actionInput.asMilestone().getDueDate();
        this.tickets = actionInput.asMilestone().getTickets();
        this.assignedDevs = actionInput.asMilestone().getAssignedDevs();

        this.createdAt = actionInput.getTimestamp();
        this.createdBy = actionInput.getUsername();
        this.status = Status.ACTIVE;
        this.blocked = false;

        //procesarea datelor de timp
        LocalDate dueDateParsed = LocalDate.parse(this.dueDate, formatter);
        LocalDate currentDate = LocalDate.parse(actionInput.getTimestamp(), formatter);
        this.daysUntilDue = Math.toIntExact(ChronoUnit.DAYS.between(currentDate, dueDateParsed));
        this.overdueBy = 0;

        this.openTickets = new ArrayList<>();
        this.closedTickets = new ArrayList<>();

        if (this.tickets != null) {
            for (int ticketId : this.tickets) {
                this.openTickets.add(ticketId);
            }
        }

        this.completionPercentage = 0.0;

        this.repartition = new ArrayList<>();
        if (this.assignedDevs != null) {
            for (String devName : this.assignedDevs) {
                if(userManger.userExists(devName)
                        && Role.DEVELOPER.equals(userManger.getRole(devName))) {
                    this.repartition.add(new DeveloperRepartition(devName));
                }
            }
        }
    }
}
