package main.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.BusinessPriority;
import enums.Status;
import fileio.ActionInput;
import main.TicketManager;
import main.tickets.Ticket;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomerImpactReportGenerator {
    private final Map<Integer, Ticket> ticketIdMap;

    public CustomerImpactReportGenerator(final TicketManager ticketManager) {
        this.ticketIdMap = ticketManager.getTicketIdMap();
    }

    public void generateCustomerImpactReport(final ActionInput actionInput,
                                             final ObjectNode response,
                                             final ObjectMapper mapper) {

        List<Ticket> activeTickets = ticketIdMap.values().stream()
                .filter(t -> t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS)
                .toList();

        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("totalTickets", activeTickets.size());

        // ticketsByType
        ObjectNode ticketsByTypeNode = mapper.createObjectNode();
        Map<String, Long> ticketsByType = activeTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getType, Collectors.counting()));
        List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK").forEach(type ->
                ticketsByTypeNode.put(type, ticketsByType.getOrDefault(type, 0L))
        );
        reportNode.set("ticketsByType", ticketsByTypeNode);

        // ticketsByPriority
        ObjectNode ticketsByPriorityNode = mapper.createObjectNode();
        Map<BusinessPriority, Long> ticketsByPriority = activeTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getBusinessPriority, Collectors.counting()));
        for (BusinessPriority p : BusinessPriority.values()) {
            ticketsByPriorityNode.put(p.name(), ticketsByPriority.getOrDefault(p, 0L));
        }
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        // customerImpactByType (simplified with polymorphism)
        ObjectNode customerImpactByTypeNode = mapper.createObjectNode();
        Map<String, Double> avgImpactByType = activeTickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getType,
                        Collectors.averagingDouble(Ticket::calculateImpact)
                ));

        List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK").forEach(type -> {
            double averageImpact = avgImpactByType.getOrDefault(type, 0.0);
            customerImpactByTypeNode.put(type, Math.round(averageImpact * 100.0) / 100.0);
        });
        reportNode.set("customerImpactByType", customerImpactByTypeNode);

        response.set("report", reportNode);
    }
}
