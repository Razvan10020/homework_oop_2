package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.BusinessPriority;
import enums.Role;
import fileio.TicketInput.ActionInput;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TicketManager {

    private final List<Ticket> tickets = new ArrayList<>();
    private int ticketIdCounter = 0;
    private LocalDate testingPhaseEnd = null; // va fi setat dupa primul ticket

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Raporteaza un ticket. Returneaza mesaj de eroare daca e cazul,
     * altfel returneaza null.
     */
    public String reportTicket(ActionInput actionInput, UserManger userManger, ObjectNode response) {
        // rapoarte anonime se pot face doar daca e BUG
        if (actionInput.getParams().getReportedBy().equals("")
                && !("BUG".equals(actionInput.getParams().getType()))) {
            return "Anonymous reports are only allowed for tickets of type BUG.";
        }

        // verifica existenta userului
        if (!userManger.userExists(actionInput.getUsername())) {
            return "The user " + actionInput.getUsername() + " does not exist.";
        }

        // initializeaza faza de testare daca nu e setata
        if (testingPhaseEnd == null) {
            LocalDate firstDate = LocalDate.parse(actionInput.getTimestamp(), formatter);
            testingPhaseEnd = firstDate.plusDays(12); // faza de testare dureaza 12 zile
        }

        // verifica daca suntem in faza de testare
        LocalDate actionDate = LocalDate.parse(actionInput.getTimestamp(), formatter);
        if (actionDate.isAfter(testingPhaseEnd)) {
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

        // daca e reporter, lista e goala
        if (Role.REPORTER.equals(role)) {
            response.set("tickets", arrayNode);
            return;
        }

        // altfel, returneaza doar ticketele create pana la timestamp-ul actiunii
        ArrayNode ticketsArray = mapper.createArrayNode();
        LocalDate actionDate = LocalDate.parse(actionInput.getTimestamp(), formatter);

        for (Ticket t : tickets) {
            LocalDate ticketDate = LocalDate.parse(t.getCreatedAt(), formatter);
            if (!ticketDate.isAfter(actionDate)) { // include ticketul daca a fost creat inainte sau la data actiunii
                ticketsArray.add(mapper.valueToTree(t));
            }
        }

        response.set("tickets", ticketsArray);
    }

    /**
     * Optional, daca mai vrei sa verifici faza de testare separat
     */
    public boolean isInTestingPhase(String timestamp) {
        if (testingPhaseEnd == null) {
            return false;
        }
        LocalDate actionDate = LocalDate.parse(timestamp, formatter);
        return !actionDate.isAfter(testingPhaseEnd);
    }
}
