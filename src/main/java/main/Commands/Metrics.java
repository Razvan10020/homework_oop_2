package main.Commands;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import enums.BusinessPriority;
import enums.RiskGrade;
import enums.Status;
import fileio.ActionInput;
import main.TicketManager;
import main.tickets.Ticket;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Metrics {
    private final Map<Integer, Ticket> ticketIdMap;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Metrics(final TicketManager ticketManager) {
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
                        Collectors.averagingDouble(Ticket::calculateRiskScore)
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

    //ultimul tip de metric
    public void generateResolutionEfficiencyReport(final ActionInput actionInput,
                                                     final ObjectNode response,
                                                     final ObjectMapper mapper) {

        List<Ticket> resolvedTickets = ticketIdMap.values().stream()
                .filter(t -> t.getStatus() == Status.RESOLVED || t.getStatus() == Status.CLOSED)
                .toList();

        ObjectNode reportNode = mapper.createObjectNode();
        reportNode.put("totalTickets", resolvedTickets.size());

        // ticketsByType
        ObjectNode ticketsByTypeNode = mapper.createObjectNode();
        Map<String, Long> ticketsByType = resolvedTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getType, Collectors.counting()));
        List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK").forEach(type ->
                ticketsByTypeNode.put(type, ticketsByType.getOrDefault(type, 0L))
        );
        reportNode.set("ticketsByType", ticketsByTypeNode);

        // ticketsByPriority
        ObjectNode ticketsByPriorityNode = mapper.createObjectNode();
        Map<BusinessPriority, Long> ticketsByPriority = resolvedTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getBusinessPriority, Collectors.counting()));
        for (BusinessPriority p : BusinessPriority.values()) {
            ticketsByPriorityNode.put(p.name(), ticketsByPriority.getOrDefault(p, 0L));
        }
        reportNode.set("ticketsByPriority", ticketsByPriorityNode);

        // efficiencyByType
        ObjectNode efficiencyByTypeNode = mapper.createObjectNode();
        Map<String, Double> avgEfficiencyByType = resolvedTickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getType,
                        Collectors.averagingDouble(this::calculateNormalizedEfficiency)
                ));

        List.of("BUG", "FEATURE_REQUEST", "UI_FEEDBACK").forEach(type -> {
            double averageEfficiency = avgEfficiencyByType.getOrDefault(type, 0.0);
            efficiencyByTypeNode.put(type, Math.round(averageEfficiency * 100.0) / 100.0);
        });
        reportNode.set("efficiencyByType", efficiencyByTypeNode);

        response.set("report", reportNode);
    }

    private double calculateNormalizedEfficiency(Ticket ticket) {
        if (ticket.getAssignedAt() == null || ticket.getAssignedAt().isEmpty() ||
            ticket.getSolvedAt() == null || ticket.getSolvedAt().isEmpty()) {
            return 0.0;
        }

        LocalDate assignedDate = LocalDate.parse(ticket.getAssignedAt(), formatter);
        LocalDate solvedDate = LocalDate.parse(ticket.getSolvedAt(), formatter);
        long daysToResolve = ChronoUnit.DAYS.between(assignedDate, solvedDate) + 1;

        double baseScore = ticket.calculateEfficiencyScore(daysToResolve);

        double maxScore = switch (ticket.getType()) {
            case "BUG" -> 70.0;
            case "FEATURE_REQUEST", "UI_FEEDBACK" -> 20.0;
            default -> 1.0; // Should not happen
        };

        return Math.min(100.0, (baseScore * 100.0) / maxScore);
    }
}