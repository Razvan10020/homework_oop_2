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
import java.util.ArrayList;
import java.util.List;

public class TicketManager {
    private TestingPhase testingPhase = new TestingPhase();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
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
        Milestone milestone = new Milestone(actionInput, userManger);
        milestones.add(milestone);
        return null;
    }

    public void ViewMilestones(ActionInput actionInput, UserManger userManger,
                               ObjectNode response, ObjectMapper mapper) {
        ArrayNode milestonesArray = mapper.createArrayNode();

        for (Milestone m : milestones) {
            milestonesArray.add(mapper.valueToTree(m));
        }
        response.set("milestones", milestonesArray);
    }

}
