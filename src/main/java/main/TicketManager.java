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
import java.util.*;
import java.util.stream.Collectors;

public class TicketManager {
    private TestingPhase testingPhase = new TestingPhase();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
    private final ArrayNode emptyArray =  new ObjectMapper().createArrayNode();
    private int ticketIdCounter = 0;

    //crearea unei harti a ticketelor bazata pe id
    Map<Integer, Ticket> ticketIdMap = tickets.stream()
            .collect(Collectors.toMap(
                    Ticket::getId,
                    ticket -> ticket
            ));

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
        ticketIdMap.put(ticket.getId(), ticket);

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
        LocalDate actionDate = LocalDate.parse(actionInput.getTimestamp(), testingPhase.getFormatter());

        //update tickets daca tot
        updateTickets(tickets, actionDate);

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
        //verifica daca s-a terminat testing phase-ul
        if (!LocalDate.parse(actionInput.getTimestamp()).isAfter(testingPhase.getTestingPhaseEndDate())) {
            return "Milestones can only be created after a testing phase has ended.";
        }

        //verifica rolurile pentru user
        Role role = userManger.getRole(actionInput.getUsername());

        if (!Role.MANAGER.equals(role)) {
            return "The user does not have permission to execute this command: required role MANAGER; user role " + role.toString() +".";
        }

        //verifica daca ticketul din milstoneInput se afla in alt milestone
        if (actionInput.asMilestone().getTickets() != null) {
            for (Integer i : actionInput.asMilestone().getTickets()) {
                if (!ticketIdMap.containsKey(i)) {
                    return "Ticket " + i + " does not exist.";
                }

                if (ticketIdMap.containsKey(i)
                        && !ticketIdMap.get(i).getAssignedMilestone().equals("")) {
                    String s = "Tickets " + i + " already assigned to milestone " + ticketIdMap.get(i).getAssignedMilestone() + ".";
                    return s;
                }
            }
        }

        Milestone milestone = new Milestone(actionInput, userManger);
        milestones.add(milestone);
        milestones.sort(
                Comparator.comparing(e ->
                        LocalDate.parse(e.getDueDate(), testingPhase.getFormatter())
        ));

        //adaugarea blocked
        if (milestone.getBlockingFor() != null) {
            for (String blockingFor : milestone.getBlockingFor()) {
                milestones.stream()
                        .filter(e -> blockingFor.equals(e.getName()))
                        .findFirst().ifPresent(e -> {
                            //e este milestonul blocat
                            e.setBlocked(true);
                            for (Integer i : e.getTickets()) {
                                if (ticketIdMap.containsKey(i)) {
                                    ticketIdMap.get(i).setBlockedDate(actionInput.getTimestamp());
                                }
                            }
                        });
            }
        }

        //schimbarea assignet to a ticketului odaca cu crearea milestonului
        //inceperea datei de schimbare a prioritatii de bussines
        if (milestone.getTickets() != null) {
            for (Integer i : milestone.getTickets()) {
                ticketIdMap.get(i).setLastUpdatedDay(milestone.getCreatedAt());
                ticketIdMap.get(i).setAssignedMilestone(milestone.getName());
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
            LocalDate due = LocalDate.parse(milestone.getDueDate(), testingPhase.getFormatter());

            if (currentDate.isAfter(due)) {
                milestone.setDaysUntilDue(0);
                milestone.setOverdueBy((int) ChronoUnit.DAYS.between(due, currentDate) + 1);
            } else {
                milestone.setDaysUntilDue((int) ChronoUnit.DAYS.between(currentDate, due) + 1);
                milestone.setOverdueBy(0);
            }
        }

    }

    public void updateTickets (List<Ticket> tickets, LocalDate currentDate) {
        for (Ticket ticket : tickets) {
            if (ticket.getLastUpdatedDay() == null) {
                ticket.setLastUpdatedDay(currentDate.format(testingPhase.getFormatter()));
            }

            Milestone parent = milestones.stream()
                    .filter(m -> m.getName().equals(ticket.getAssignedMilestone()))
                    .findFirst().orElse(null);

            if (parent != null && parent.isBlocked()) {
                ticket.setLastUpdatedDay(currentDate.format(testingPhase.getFormatter()));
                continue;
            }

            LocalDate lastUpdatedDay = LocalDate.parse(ticket.getLastUpdatedDay(), testingPhase.getFormatter());

            int daysPassed = (int) ChronoUnit.DAYS.between(lastUpdatedDay, currentDate);
            int totalDaysAvailable = daysPassed + ticket.getChangeDaysAgo();;

            if (parent != null) {
                LocalDate dueDate = LocalDate.parse(parent.getDueDate(), testingPhase.getFormatter());
                int diffToDue = (int) ChronoUnit.DAYS.between(currentDate, dueDate) + 1;

                if (diffToDue <= 1) {
                    ticket.setBusinessPriority(BusinessPriority.CRITICAL);
                    totalDaysAvailable = 0;
                } else if (diffToDue == 2) {
                    ticket.setBusinessPriority(BusinessPriority.CRITICAL);
                    totalDaysAvailable = 0;
                }
            }
            while (totalDaysAvailable >= 3) {
                if (ticket.getBusinessPriority() == BusinessPriority.CRITICAL) {
                    totalDaysAvailable= 0;
                    break;
                }

                switch (ticket.getBusinessPriority()) {
                    case LOW ->  {
                        ticket.setBusinessPriority(BusinessPriority.MEDIUM);
                    }
                    case MEDIUM -> {
                        ticket.setBusinessPriority(BusinessPriority.HIGH);
                    }
                    case  HIGH ->  {
                        ticket.setBusinessPriority(BusinessPriority.CRITICAL);
                    }
                    default -> {}

                }
                totalDaysAvailable -= 3;
            }
            ticket.setLastUpdatedDay(currentDate.format(testingPhase.getFormatter()));
            ticket.setChangeDaysAgo(totalDaysAvailable);
        }
    }
}
