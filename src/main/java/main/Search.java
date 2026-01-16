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
import main.utils.TicketSearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Search {
    private final TicketManager ticketManager;
    private final List<Milestone> milestones;
    private final Map<Integer, Ticket> ticketIdMap;

    public Search(final TicketManager ticketManager) {
        this.ticketManager = ticketManager;
        this.milestones = ticketManager.getMilestones();
        this.ticketIdMap = ticketManager.getTicketIdMap();
    }

    public void search(final ActionInput actionInput, final UserManger userManger,
                       final ObjectNode response, final ObjectMapper mapper) {

        String username = actionInput.getUsername();
        User currentUser = userManger.getUser(username);
        Role role = currentUser.getRole();
        FilterInput filter = actionInput.asFilter();

        if (filter == null) {
            ticketManager.ViewTicket(actionInput, userManger, response, mapper);
            return;
        }

        if (filter.getSearchType() != null) {
            response.put("searchType", filter.getSearchType().toString());
        }

        if (role.equals(Role.DEVELOPER)) {
            searchTicketsForDevs(actionInput, userManger, response);
        } else if (role.equals(Role.MANAGER)) {
            if (filter.isDeveloperSearch()) {
                searchDevelopers(actionInput, userManger, response);
            } else {
                searchTicketsForManager(actionInput, userManger, response, mapper);
            }
        }
    }

    private void searchTicketsForDevs(final ActionInput actionInput,
                                      final UserManger userManger, final ObjectNode response) {
        String username = actionInput.getUsername();
        List<Ticket> pool = getDeveloperInitialTickets(username);
        FilterInput filter = actionInput.asFilter();

        List<TicketSearchResult> results = pool.stream()
                .filter(t -> filter.getType() == null
                        || filter.getType().equals(t.getType()))
                .filter(t -> filter.getBusinessPriority() == null
                        || filter.getBusinessPriority().equals(t.getBusinessPriority()))
                .filter(t -> filter.getCreatedAt() == null
                        || filter.getCreatedAt().equals(t.getCreatedAt()))
                .filter(t -> filter.getCreatedBefore() == null
                        || isBefore(t.getCreatedAt(), filter.getCreatedBefore()))
                .filter(t -> filter.getCreatedAfter() == null
                        || isAfter(t.getCreatedAt(), filter.getCreatedAfter()))
                .filter(t -> filter.getAvailableForAssignment() == null
                        || filter.getAvailableForAssignment()
                        .equals(isAvailable(t, username, userManger)))
                .sorted(ticketComparator())
                .map(t -> new TicketSearchResult(t, null))
                .collect(Collectors.toList());

        response.putPOJO("results", results);

    }

    private List<Ticket> getDeveloperInitialTickets(final String username) {
        return milestones.stream()
                .filter(m -> m.getAssignedDevs() != null
                        && java.util.Arrays.asList(m.getAssignedDevs()).contains(username))
                .flatMapToInt(m -> java.util.Arrays.stream(m.getTickets()))
                .mapToObj(ticketIdMap::get)
                .filter(t -> t != null && t.getStatus() == Status.OPEN)
                .collect(Collectors.toList());
    }

    private boolean isBefore(final String ticketDate, final String filterDate) {
        return java.time.LocalDate.parse(ticketDate).isBefore(java.time.LocalDate.parse(filterDate));
    }

    private boolean isAfter(final String ticketDate, final String filterDate) {
        return java.time.LocalDate.parse(ticketDate).isAfter(java.time.LocalDate.parse(filterDate));
    }

    /**
     * Verifica disponibilitatea pentru asignare.
     * Redenumit din AvaidableForAssignement pentru Checkstyle si typos.
     */
    private boolean isAvailable(final Ticket ticket,
                                final String username, final UserManger userManger) {
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

    private void searchTicketsForManager(final ActionInput actionInput,
                                         final UserManger userManger,
                                         final ObjectNode response, final ObjectMapper mapper) {
        FilterInput filter = actionInput.asFilter();
        String username = actionInput.getUsername();
        // Managerul pleacă de la TOATE tichetele
        List<Ticket> pool = new ArrayList<>(ticketIdMap.values());

        List<TicketSearchResult> results = pool.stream()
                .filter(t -> filter.getType() == null
                        || filter.getType().equals(t.getType()))
                .filter(t -> filter.getBusinessPriority() == null
                        || filter.getBusinessPriority().equals(t.getBusinessPriority()))
                .filter(t -> filter.getKeywords() == null
                        || matchesKeywords(t, filter.getKeywords()))
                .filter(t -> filter.getCreatedAt() == null
                        || filter.getCreatedAt().equals(t.getCreatedAt()))
                .filter(t -> filter.getCreatedBefore() == null
                        || isBefore(t.getCreatedAt(), filter.getCreatedBefore()))
                .filter(t -> filter.getCreatedAfter() == null
                        || isAfter(t.getCreatedAt(), filter.getCreatedAfter()))
                .filter(t -> filter.getAvailableForAssignment() == null
                        || filter.getAvailableForAssignment()
                        .equals(isAvailable(t, username, userManger)))
                .sorted(ticketComparator())
                .map(t -> {
                    List<String> matchingWords = null;
                    if (filter.getKeywords() != null) {
                        matchingWords = getMatchingWords(t, filter.getKeywords());
                    }
                    return new TicketSearchResult(t, matchingWords);
                })
                .collect(Collectors.toList());

        response.putPOJO("results", results);
    }

    private void searchDevelopers(final ActionInput actionInput,
                                  final UserManger userManger, final ObjectNode response) {
        FilterInput filter = actionInput.asFilter();
        // Presupunem că ai o metodă în UserManager care îți dă lista de dev pentru un manager
        List<String> poolFirst =
                ((Manager) userManger.getUser(actionInput.getUsername())).getSubordinates();

        List<Developer> pool = new ArrayList<>();
        for (String username : poolFirst) {
            pool.add((Developer) userManger.getUser(username));
        }

        List<Developer> results = pool.stream()
                .filter(d -> filter.getExpertiseArea() == null
                        || d.getExpertiseArea().equals(filter.getExpertiseArea()))
                .filter(d -> filter.getSeniority() == null
                        || d.getSeniority().equals(filter.getSeniority()))
                .filter(d -> filter.getPerformanceScoreAbove() == null
                        || d.getPerformanceScore() >= filter.getPerformanceScoreAbove())
                .filter(d -> filter.getPerformanceScoreBelow() == null
                        || d.getPerformanceScore() < filter.getPerformanceScoreBelow())
                .sorted(developerComparator())
                .collect(Collectors.toList());

        response.putPOJO("results", results);
    }

    private boolean matchesKeywords(final Ticket t, final List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }
        // Concatenăm titlul și descrierea și facem totul lowercase
        String description = t.getDescription() == null ? "" : t.getDescription();
        String searchBase = (t.getTitle() + " " + description).toLowerCase();

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

    private List<String> getMatchingWords(final Ticket t, final List<String> keywords) {
        if (keywords == null) {
            return new ArrayList<>();
        }
        String searchBase = (t.getTitle() + " " + t.getDescription()).toLowerCase();

        return keywords.stream()
                .filter(k -> searchBase.contains(k.toLowerCase()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
