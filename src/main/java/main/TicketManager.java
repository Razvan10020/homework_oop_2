package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.BusinessPriority;
import enums.ExpertiseArea;
import enums.Role;
import enums.Seniority;
import enums.Status;
import fileio.ActionInput;
import fileio.ActionComsIn.AddCommentInput;
import fileio.ActionComsIn.AssignTicketInput;
import fileio.ActionComsIn.MilestoneInput;
import main.Tickets.Comment;
import main.Tickets.Milestone;
import main.Tickets.TestingPhase;
import main.Users.Developer;
import main.Users.DeveloperRepartition;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import main.utils.Views; // Added import

public class TicketManager {
    private TestingPhase testingPhase = new TestingPhase();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
    private final ArrayNode emptyArray =  new ObjectMapper().createArrayNode();
    private int ticketIdCounter = 0;

    //crearea hartii de repartitie
    private final List<DeveloperRepartition> developers = new ArrayList<DeveloperRepartition>();
    Map<String, DeveloperRepartition> developersMap = developers.stream()
            .collect(Collectors.toMap(
                    DeveloperRepartition::getDeveloper,
                    developer -> developer
            ));

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

            if (Role.DEVELOPER.equals(role)) {
                if (t.getStatus() != Status.OPEN) {
                    continue;
                }
                // Check if developer is assigned to the milestone of the ticket
                if (!isDevAssignedToMilestone(actionInput.getUsername(), t.getAssignedMilestone())) {
                    continue;
                }
            }

            LocalDate ticketDate = LocalDate.parse(t.getCreatedAt(), testingPhase.getFormatter());
            if (!ticketDate.isAfter(actionDate)) { // include ticketul daca a fost creat inainte sau la data actiunii
                try {
                    ticketsArray.add(mapper.readTree(mapper.writerWithView(Views.GeneralTicketView.class).writeValueAsString(t)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

            // Update ticket lists
            List<Integer> open = new ArrayList<>();
            List<Integer> closed = new ArrayList<>();

            if (milestone.getTickets() != null) {
                for (int ticketId : milestone.getTickets()) {
                    Ticket t = ticketIdMap.get(ticketId);
                    if (t != null) {
                        if (t.getStatus() == Status.RESOLVED || t.getStatus() == Status.CLOSED) {
                            closed.add(ticketId);
                        } else {
                            open.add(ticketId);
                        }
                    }
                }
            }
            milestone.setOpenTickets(open);
            milestone.setClosedTickets(closed);

            // Update completion percentage
            int total = (milestone.getTickets() != null) ? milestone.getTickets().length : 0;
            if (total > 0) {
                milestone.setCompletionPercentage((double) closed.size() / total);
            } else {
                milestone.setCompletionPercentage(0.0);
            }

            // Update repartition
            List<DeveloperRepartition> repartitions = new ArrayList<>();
            if (milestone.getAssignedDevs() != null) {
                for (String devUsername : milestone.getAssignedDevs()) {
                    DeveloperRepartition devRep = new DeveloperRepartition(devUsername);
                    if (milestone.getTickets() != null) {
                        for (int ticketId : milestone.getTickets()) {
                            Ticket t = ticketIdMap.get(ticketId);
                            // Only count if ticket is assigned to this dev
                            if (t != null && devUsername.equals(t.getAssignedTo())) {
                                devRep.getAssignedTickets().add(ticketId);
                            }
                        }
                    }
                    repartitions.add(devRep);
                }
            }
            milestone.setRepartition(repartitions);
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

    public String assignTicket(ActionInput actionInput, UserManger userManger, ObjectNode response) {
        String username = actionInput.getUsername();
        int ticketId = actionInput.asAssignTicket().getTicketId();
        Ticket ticket = ticketIdMap.get(ticketId);

        if (ticket == null) {
            return "Ticket does not exist.";
        }

        Developer developer = (Developer) userManger.getUser(username);

        //Check Expertise
        if (!canHandleExpertise(developer, ticket)) {
            String required = getRequiredExpertiseString(ticket.getExpertiseArea());
            return "Developer " + username + " cannot assign ticket " + ticketId + " due to expertise area. Required: " + required + "; Current: " + developer.getExpertiseArea() + ".";
        }

        // Check Seniority
        if (!canHandleSeniority(developer, ticket)) {
            String required = getRequiredSeniorityString(ticket.getBusinessPriority());
            return "Developer " + username + " cannot assign ticket " + ticketId + " due to seniority level. Required: " + required + "; Current: " + developer.getSeniority() + ".";
        }

        // 3. Check Status
        if (ticket.getStatus() != Status.OPEN) {
            return "Only OPEN tickets can be assigned.";
        }

        // 4. Check Milestone Assignment
        String assignedMilestoneName = ticket.getAssignedMilestone();
        if (assignedMilestoneName == null || assignedMilestoneName.isEmpty()) {
            return "Ticket is not assigned to any milestone.";
        }
        if (!isDevAssignedToMilestone(username, assignedMilestoneName)) {
            return "Developer " + username + " is not assigned to milestone " + assignedMilestoneName + ".";
        }

        // 5. Check Blocked Milestone
        if (isMilestoneBlocked(assignedMilestoneName)) {
            return "Cannot assign ticket " + ticketId + " from blocked milestone " + assignedMilestoneName + ".";
        }

        // SUCCESS
        ticket.setStatus(Status.IN_PROGRESS);
        ticket.setAssignedAt(actionInput.getTimestamp());
        ticket.setAssignedTo(username);

        DeveloperRepartition devRep = developersMap.get(username);
        if (devRep == null) {
            devRep = new DeveloperRepartition(username);
            developersMap.put(username, devRep);
            developers.add(devRep);
        }

        if (!devRep.getAssignedTickets().contains(ticketId)) {
            devRep.getAssignedTickets().add(ticketId);
        }

        return null;
    }

    public String undoAssignTicket(ActionInput actionInput, UserManger userManger, ObjectNode response) {
        String username = actionInput.getUsername();
        int ticketId = actionInput.asAssignTicket().getTicketId();
        Ticket ticket = ticketIdMap.get(ticketId);

        if (ticket == null) {
            // Should exist as per requirements guarantee, but good to check
            return null;
        }

        if (ticket.getStatus() != Status.IN_PROGRESS) {
             return "The ticket " + ticketId + " is not IN_PROGRESS."; // Or "Only IN_PROGRESS tickets can be unassigned."
        }

        ticket.setStatus(Status.OPEN);
        ticket.setAssignedTo(""); // Clear assignment
        ticket.setAssignedAt(""); // Reset assignedAt as well
        
        // Remove from developer repartition?
        DeveloperRepartition devRep = developersMap.get(username);
        if (devRep != null) {
            devRep.getAssignedTickets().remove(Integer.valueOf(ticketId));
        }

        return null;
    }


    public void ViewAssignedTickets(ActionInput actionInput, UserManger userManger, ObjectNode response, ObjectMapper mapper) {
        ArrayNode assignedTicketsArray = mapper.createArrayNode();
        String username = actionInput.getUsername();
        ObjectMapper objectMapper = new ObjectMapper();

        // Update tickets before viewing
        updateTickets(tickets, LocalDate.parse(actionInput.getTimestamp()));

        // Obținem lista de ID-uri de la developer
        DeveloperRepartition devRep = developersMap.get(username);
        if (devRep == null) {
             response.set("assignedTickets", assignedTicketsArray);
             return;
        }
        
        List<Integer> ticketIds = devRep.getAssignedTickets();

        if (ticketIds != null) {
            List<Ticket> ticketsToDisplay = new ArrayList<>();
            for (Integer id : ticketIds) {
                if (ticketIdMap.containsKey(id)) {
                    ticketsToDisplay.add(ticketIdMap.get(id));
                }
            }

            // Sortarea (Prioritate desc, Data asc, ID asc)
            ticketsToDisplay.sort((t1, t2) -> {
                int p1 = getPriorityValue(t1.getBusinessPriority());
                int p2 = getPriorityValue(t2.getBusinessPriority());
                if (p1 != p2) return Integer.compare(p2, p1);

                int dateComp = t1.getCreatedAt().compareTo(t2.getCreatedAt());
                if (dateComp != 0) return dateComp;

                return Integer.compare(t1.getId(), t2.getId());
            });

            for (Ticket t : ticketsToDisplay) {
                try {
                    assignedTicketsArray.add(mapper.readTree(mapper.writerWithView(Views.AssignedTicketView.class).writeValueAsString(t)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        response.set("assignedTickets", assignedTicketsArray);
    }


    private int getPriorityValue(BusinessPriority priority) {
        return switch (priority) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

    // Helper methods for Assignment Logic

    private boolean canHandleExpertise(Developer dev, Ticket ticket) {
        ExpertiseArea devArea = dev.getExpertiseArea();
        ExpertiseArea ticketArea = ticket.getExpertiseArea();
        
        if (devArea == ExpertiseArea.FULLSTACK) return true;
        if (devArea == ticketArea) return true;
        
        // Specific mapping
        if (devArea == ExpertiseArea.FRONTEND && ticketArea == ExpertiseArea.DESIGN) return true;
        if (devArea == ExpertiseArea.DESIGN && ticketArea == ExpertiseArea.FRONTEND) return true;
        if (devArea == ExpertiseArea.BACKEND && ticketArea == ExpertiseArea.DB) return true;
        
        return false;
    }

    private String getRequiredExpertiseString(ExpertiseArea area) {
        List<String> areas = new ArrayList<>();
        areas.add(area.toString());
        areas.add("FULLSTACK");

        if (area == ExpertiseArea.FRONTEND) areas.add("DESIGN");
        if (area == ExpertiseArea.DESIGN) areas.add("FRONTEND");
        if (area == ExpertiseArea.DB) areas.add("BACKEND");

        Collections.sort(areas);
        return String.join(", ", areas);
    }

    private boolean canHandleSeniority(Developer dev, Ticket ticket) {
        BusinessPriority priority = ticket.getBusinessPriority();
        Seniority seniority = dev.getSeniority();

        switch (seniority) {
            case JUNIOR:
                return priority == BusinessPriority.LOW || priority == BusinessPriority.MEDIUM;
            case MID:
                return priority == BusinessPriority.LOW || priority == BusinessPriority.MEDIUM || priority == BusinessPriority.HIGH;
            case SENIOR:
                return true; // All
            default:
                return false;
        }
    }

    private String getRequiredSeniorityString(BusinessPriority priority) {
        List<String> levels = new ArrayList<>();
        // Logic: Inverse of canHandleSeniority
        // LOW: JUNIOR, MID, SENIOR
        // MEDIUM: JUNIOR, MID, SENIOR
        // HIGH: MID, SENIOR
        // CRITICAL: SENIOR
        
        levels.add("SENIOR"); // Always capable
        if (priority != BusinessPriority.CRITICAL) {
            levels.add("MID");
        }
        if (priority == BusinessPriority.LOW || priority == BusinessPriority.MEDIUM) {
            levels.add("JUNIOR");
        }
        
        Collections.sort(levels);
        return String.join(", ", levels);
    }

    private boolean isDevAssignedToMilestone(String username, String milestoneName) {
        if (milestoneName == null || milestoneName.isEmpty()) return false;
        for (Milestone m : milestones) {
            if (m.getName().equals(milestoneName)) {
                if (m.getAssignedDevs() == null) return false;
                for (String dev : m.getAssignedDevs()) {
                    if (dev.equals(username)) return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean isMilestoneBlocked(String milestoneName) {
         if (milestoneName == null || milestoneName.isEmpty()) return false;
        for (Milestone m : milestones) {
            if (m.getName().equals(milestoneName)) {
                return m.isBlocked();
            }
        }
        return false;
    }

    public String addComment(ActionInput actionInput, UserManger userManger, ObjectNode response) {
        Role role = userManger.getRole(actionInput.getUsername());
        AddCommentInput commentInput = actionInput.asAddComment();
        if (commentInput == null) return null;

        Ticket ticket = ticketIdMap.get(commentInput.getTicketID());

        if (ticket == null) {
            return null;
        }

        if (ticket.getReportedBy().isEmpty()) {
            return "Comments are not allowed on anonymous tickets.";
        }

        if (role == Role.REPORTER && ticket.getStatus() == Status.CLOSED) {
            return "Reporters cannot comment on CLOSED tickets.";
        }

        if (commentInput.getComment().length() < 10) {
            return "Comment must be at least 10 characters long.";
        }

        if (role == Role.DEVELOPER) {
            if (!actionInput.getUsername().equals(ticket.getAssignedTo())) {
                return "Ticket " + ticket.getId() + " is not assigned to the developer " + actionInput.getUsername() + ".";
            }
        }

        if (role == Role.REPORTER) {
            if (!actionInput.getUsername().equals(ticket.getReportedBy())) {
                return "Reporter " + actionInput.getUsername() + " cannot comment on ticket " + ticket.getId() + ".";
            }
        }

        Comment comment = new Comment(
                actionInput.getUsername(),
                commentInput.getComment(),
                actionInput.getTimestamp()
        );

        ticket.addCommentToTicket(comment);
        return null;
    }

    public String undoAddComment(ActionInput actionInput, UserManger userManger, ObjectNode response) {
        AddCommentInput undoInput = actionInput.asUndoAddComment();
        if (undoInput == null) return null;

        Ticket ticket = ticketIdMap.get(undoInput.getTicketID());
        if (ticket == null) {
            return null;
        }

        if (ticket.getReportedBy().isEmpty()) {
            return "Comments are not allowed on anonymous tickets.";
        }

        List<Comment> comments = ticket.getComments();
        for (int i = comments.size() - 1; i >= 0; i--) {
            if (comments.get(i).getAuthor().equals(actionInput.getUsername())) {
                comments.remove(i);
                return null;
            }
        }

        return null;
    }
}
