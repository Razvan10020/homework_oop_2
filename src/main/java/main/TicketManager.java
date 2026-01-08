package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.BusinessPriority;
import enums.Role;
import fileio.ActionInput;
import main.Tickets.Milestone;
import main.Tickets.TestingPhase;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TicketManager {
    private TestingPhase testingPhase = new TestingPhase();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
    private final ArrayNode emptyArray =  new ObjectMapper().createArrayNode();
    private int ticketIdCounter = 0;

    /**
     * Raporteaza un ticket. Returneaza mesaj de eroare daca e cazul,
     * altfel returneaza null.
     */
    public String reportTicket(ActionInput actionInput,
                               UserManger userManger, ObjectNode response) {
        // rapoarte anonime se pot face doar daca e BUG
        if (actionInput.asParams().getReportedBy().equals("")
                && !("BUG".equals(actionInput.asParams().getType()))) {
            return "Anonymous reports are only allowed for tickets of type BUG.";
        }

        // verifica existenta userului
        if (!userManger.userExists(actionInput.getUsername())) {
            return "The user " + actionInput.getUsername() + " does not exist.";
        }

        // initializeaza faza de testare daca nu e setata
        if (testingPhase.getTestingPhaseEndDate() == null) {
            testingPhase.setTestingPhase(actionInput.getTimestamp());// faza de testare dureaza 12 zile
        }

        // verifica daca suntem in faza de testare
        if (!testingPhase.isInTestingPhase(actionInput.getTimestamp())) {
            return "Tickets can only be reported during testing phases.";
        }

        // adauga ticket
        Ticket ticket = new Ticket(ticketIdCounter++, actionInput);
        if(ticket.getReportedBy().isEmpty()){
            ticket.setBusinessPriority(BusinessPriority.LOW);
        }

        tickets.add(ticket);

        return null; // totul ok
    }

    /**
     * Creeaza lista de tickets pentru comanda viewTickets
     */
    public void ViewTicket(ActionInput actionInput, UserManger userManger,
                           ObjectNode response, ObjectMapper mapper) {
        Role role = userManger.getRole(actionInput.getUsername());
        ArrayNode arrayNode = mapper.createArrayNode();
        //
        // daca e reporter, lista e goala
        if (Role.REPORTER.equals(role)) {
            response.set("tickets", arrayNode);
            return;
        }

        // altfel, returneaza doar ticketele create pana la timestamp-ul actiunii
        ArrayNode ticketsArray = mapper.createArrayNode();
        LocalDate actionDate = LocalDate.parse(actionInput.getTimestamp(), testingPhase.getFormatter());

        for (Ticket t : tickets) {
            LocalDate ticketDate = LocalDate.parse(t.getCreatedAt(), testingPhase.getFormatter());
            if (!ticketDate.isAfter(actionDate)) { // include ticketul daca a fost creat inainte sau la data actiunii
                ticketsArray.add(mapper.valueToTree(t));
            }
        }

        response.set("tickets", ticketsArray);
    }

    public String createMilestone(ActionInput actionInput, UserManger userManger,
                                  ObjectNode response) {
        if (!LocalDate.parse(actionInput.getTimestamp()).isAfter(testingPhase.getTestingPhaseEndDate())) {
            return "Milestones can only be created after a testing phase has ended.";
        }

        Milestone milestone = new Milestone(actionInput, userManger);
        milestones.add(milestone);
        milestones.sort(
                Comparator.comparing(e ->
                        LocalDate.parse(e.getDueDate(), testingPhase.getFormatter())
        ));

        if (milestone.getBlockingFor() != null) {
            for (String blockingFor : milestone.getBlockingFor()) {
                milestones.stream()
                        .filter(e -> blockingFor.equals(e.getName()))
                        .findFirst().ifPresent(e -> {
                            e.setBlocked(true);
                        });
            }
        }
        return null;
    }

    public void ViewMilestones(ActionInput actionInput, UserManger userManger,
                               ObjectNode response, ObjectMapper mapper) {
        ArrayNode milestonesArray = mapper.createArrayNode();

        updateMilestones(milestones, LocalDate.parse(actionInput.getTimestamp()));

        if (Role.REPORTER.equals(userManger.getRole(actionInput.getUsername()))) {
            response.set("milestones", emptyArray);
            return;
        }

        Role role = userManger.getRole(actionInput.getUsername());

        for (Milestone m : milestones) {
            if (Role.MANAGER.equals(role) && m.getCreatedBy().equals(actionInput.getUsername())) {
                milestonesArray.add(mapper.valueToTree(m));
            }

            if (Role.DEVELOPER.equals(role)
                    && Arrays.asList(m.getAssignedDevs()).contains(actionInput.getUsername())) {
                milestonesArray.add(mapper.valueToTree(m));
            }
        }
        response.set("milestones", milestonesArray);
    }

    public void updateMilestones(List<Milestone> milestones, LocalDate currentDate) {
        for (Milestone milestone : milestones) {
            LocalDate dueDateParsed = LocalDate.parse(milestone.getDueDate(), testingPhase.getFormatter());
            milestone.setDaysUntilDue(
                    Math.toIntExact(
                            ChronoUnit.DAYS.between(currentDate, dueDateParsed)) + 1
            );
        }

    }
}
