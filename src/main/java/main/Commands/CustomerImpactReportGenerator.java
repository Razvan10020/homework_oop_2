package main.Commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.BusinessPriority;
import enums.RiskGrade;
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
        if (actionInput.getCommand().equals("generateCustomerImpactReport")) {
            customerImpactByTypeNode(reportNode, activeTickets, mapper);
        }
        if (actionInput.getCommand().equals("generateTicketRiskReport")) {
            ticketRiskReportNode(reportNode, activeTickets, mapper);
        }
        response.set("report", reportNode);
    }

    private void customerImpactByTypeNode (final ObjectNode reportNode, List<Ticket> activeTickets, ObjectMapper mapper) {
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
    }

    private void ticketRiskReportNode (final ObjectNode reportNode, final List<Ticket> activeTickets, final ObjectMapper mapper) {
        ObjectNode ticketRiskReportNode = mapper.createObjectNode();
        Map<String, Double> avgRiskByType = activeTickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getType,
                        Collectors.averagingDouble(Ticket::calculateRisk)
                ));

        List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK").forEach(type -> {
            double averageRisk = avgRiskByType.getOrDefault(type, 0.0);
            double roundedAverageRisk = Math.round(averageRisk * 100.0) / 100.0;
            RiskGrade risk = convertToGrade(roundedAverageRisk);
            ticketRiskReportNode.put(type, risk.toString());
        });

        reportNode.set("riskByType", ticketRiskReportNode);
    }

    private RiskGrade convertToGrade(final double roundedAverageRisk) {
        if (roundedAverageRisk <= 24) {
            return RiskGrade.NEGLIGIBLE;
        } else if (roundedAverageRisk <= 49) {
            return RiskGrade.MODERATE;
        } else if (roundedAverageRisk <= 74) {
            return RiskGrade.SIGNIFICANT;
        } else {
            return RiskGrade.MAJOR;
        }
    }
}
