package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.Role;
import enums.Status;
import fileio.ActionComsIn.FilterInput;
import fileio.ActionInput;
import main.Commands.Milestone;
import main.Users.Developer;
import main.Users.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Search {
    TicketManager ticketManager = new TicketManager();
    private List<Milestone> milestones = ticketManager.getMilestones();
    Map<Integer, Ticket> ticketIdMap = ticketManager.getTicketIdMap();

    public void search(final ActionInput actionInput, final UserManger userManger,
                       final ObjectNode response, final ObjectMapper mapper) {

        String username = actionInput.getUsername();
        Role role = userManger.getRole(username);
        FilterInput filter = actionInput.asFilter();

        if (filter == null) {
            ticketManager.ViewTicket(actionInput, userManger, response, mapper);
            return;
        }

        List<Ticket> pool = new ArrayList<>();
        if (Role.DEVELOPER.equals(role)) {
            pool = getDeveloperInitialTickets(username);
        }
        else if (Role.MANAGER.equals(role)) {
            pool = getManagerInitialTickets(username);
        }

        // Pipeline-ul de filtrare - CURAT ȘI COERENT
        List<Ticket> results = pool.stream()
                .filter(t -> filter.getType() == null || filter.getType().equals(t.getType()))
                .filter(t -> filter.getBusinessPriority() == null || filter.getBusinessPriority().equals(t.getBusinessPriority()))
                .filter(t -> filter.getCreatedAt() == null || filter.getCreatedAt().equals(t.getCreatedAt()))
                .filter(t -> filter.getCreatedBefore() == null || isBefore(t.getCreatedAt(), filter.getCreatedBefore()))
                .filter(t -> filter.getCreatedAfter() == null || isAfter(t.getCreatedAt(), filter.getCreatedAfter()))
                .filter(t -> filter.getAvailableForAssignment() == null
                        || filter.getAvailableForAssignment().equals(isAvailable(t, username, userManger)))
                .collect(Collectors.toList());

        response.putPOJO("results", results);
    }

    private List<Ticket> getDeveloperInitialTickets(String username) {
        return milestones.stream()
                .filter(m -> m.getAssignedDevs() != null &&
                        java.util.Arrays.asList(m.getAssignedDevs()).contains(username))
                .flatMapToInt(m -> java.util.Arrays.stream(m.getTickets()))
                .mapToObj(ticketIdMap::get)
                .filter(t -> t != null && t.getStatus() == Status.OPEN)
                .collect(Collectors.toList());
    }

    private boolean isBefore(String ticketDate, String filterDate) {
        return LocalDateTime.parse(ticketDate).isBefore(LocalDateTime.parse(filterDate));
    }

    private boolean isAfter(String ticketDate, String filterDate) {
        return LocalDateTime.parse(ticketDate).isAfter(LocalDateTime.parse(filterDate));
    }

    /**
     * Verifica disponibilitatea pentru asignare.
     * Redenumit din AvaidableForAssignement pentru Checkstyle si typos.
     */
    private boolean isAvailable(Ticket ticket, String username, UserManger userManger) {
        User user = userManger.getUser(username);
        if (!(user instanceof Developer)) {
            return false;
        }
        Developer dev = (Developer) user;

        // Logica ta ramane aceeasi, dar mai curata
        return ticketManager.canHandleExpertise(dev, ticket)
                && ticketManager.canHandleSeniority(dev, ticket)
                && !ticketManager.isMilestoneBlocked(ticket.getAssignedMilestone());
    }

    private List<Ticket> getManagerInitialTickets(String username) {

    }

}
