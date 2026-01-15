package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.Role;
import enums.Status;
import fileio.ActionComsIn.FilterInput;
import fileio.ActionInput;
import main.Commands.Milestone;
import main.Users.Developer;
import main.Users.Manager;
import main.Users.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
        User currentUser = userManger.getUser(username);
        Role role = currentUser.getRole();
        FilterInput filter = actionInput.asFilter();

        // Regula 3: Dacă nu este specificat niciun filtru → viewTickets
        if (filter == null ) {
            ticketManager.ViewTicket(actionInput, userManger, response, mapper);
            return;
        }

        if (Role.DEVELOPER.equals(role)) {
            searchTicketsForDevs(actionInput, userManger, response);
        }

        // Managerul poate căuta și DEVELOPERI
        if (Role.MANAGER.equals(role) && filter.isDeveloperSearch()) {
            searchDevelopers(actionInput, userManger, response);
        } else {
            searchTicketsForManager(actionInput, userManger, response, role);
        }
    }

    private void searchTicketsForDevs(final ActionInput actionInput, final UserManger userManger, final ObjectNode response) {
        String username = actionInput.getUsername();
        List<Ticket> pool = getDeveloperInitialTickets(username);
        FilterInput filter = actionInput.asFilter();

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

    private void searchTicketsForManager(ActionInput actionInput, final UserManger userManger,
                                         final ObjectNode response) {
        FilterInput filter = actionInput.asFilter();
        // Managerul pleacă de la TOATE tichetele
        List<Ticket> pool = new ArrayList<>(ticketIdMap.values());

        List<Ticket> results = pool.stream()
                .filter(t -> filter.getType() == null || filter.getType().equals(t.getType()))
                .filter(t -> filter.getBusinessPriority() == null || filter.getBusinessPriority().equals(t.getBusinessPriority()))
                .filter(t -> filter.getKeywords() == null || matchesKeywords(t, filter.getKeywords())) // Filtru extra de keywords
                .sorted(Comparator.comparing(Ticket::getCreatedAt).thenComparing(Ticket::getId))
                .collect(Collectors.toList());

        response.putPOJO("results", results);
    }

    private List<Ticket> getManagerInitialTickets(String username) {

    }

    private void searchDevelopers(ActionInput actionInput, UserManger userManger, ObjectNode response) {
        FilterInput filter = actionInput.asFilter();
        // Presupunem că ai o metodă în UserManager care îți dă lista de dev pentru un manager
        List<String> poolFirst = ((Manager) userManger.getUser(actionInput.getUsername())).getSubordinates();

        List<Developer> pool = new ArrayList<>();
        for (String username : poolFirst) {
            pool.add((Developer) userManger.getUser(username));
        }

        List<Developer> results = pool.stream()
                .filter(d -> filter.getExpertiseArea() == null || d.getExpertiseArea().equals(filter.getExpertiseArea()))
                .filter(d -> filter.getSeniority() == null || d.getSeniority().equals(filter.getSeniority()))
                .filter(d -> d.getPerformanceScore() >= filter.getPerformanceScoreAbove())
                .sorted(Comparator.comparing(Developer::getUsername))
                .collect(Collectors.toList());

        response.putPOJO("results", results);
    }

    private boolean matchesKeywords(Ticket t, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }
        // Concatenăm titlul și descrierea și facem totul lowercase
        String searchBase = (t.getTitle() + " " + t.getDescription()).toLowerCase();

        // Verificăm dacă măcar unul dintre cuvintele cheie se regăsește în bază
        return keywords.stream()
                .anyMatch(k -> searchBase.contains(k.toLowerCase()));
    }

    /**
     * Comparator pentru tichete: întai după data creării (vechi -> noi), apoi după ID.
     */
    private Comparator<Ticket> ticketComparator() {
        return Comparator.comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId);
    }

    /**
     * Comparator pentru developeri: alfabetic după username.
     */
    private Comparator<Developer> developerComparator() {
        return Comparator.comparing(Developer::getUsername);
    }

    private List<String> getMatchingWords(Ticket t, List<String> keywords) {
        if (keywords == null) return new ArrayList<>();
        String searchBase = (t.getTitle() + " " + t.getDescription()).toLowerCase();

        return keywords.stream()
                .filter(k -> searchBase.contains(k.toLowerCase()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
