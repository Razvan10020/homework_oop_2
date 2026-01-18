package main.visitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.BusinessPriority;
import enums.Status;
import main.tickets.Bug;
import main.tickets.FeatureRequest;
import main.tickets.Ticket;
import main.tickets.UiFeedback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A visitor that traverses tickets to generate an application stability report.
 * It calculatesx metrics like open tickets, risk, and impact.
 */
public class StabilityReportVisitor implements TicketVisitor {
    private static final double RISK_CRITICAL_THRESHOLD = 75;
    private static final double RISK_SIGNIFICANT_THRESHOLD = 50;
    private static final double RISK_MODERATE_THRESHOLD = 25;
    private static final double STABILITY_UNSTABLE_THRESHOLD = 40;
    private static final double STABILITY_NEEDS_IMPROVEMENT_THRESHOLD = 20;
    private static final double PERCENTAGE_MULTIPLIER = 100.0;


    private int totalOpenTickets = 0;
    private final Map<String, Integer> openTicketsByType = new HashMap<>();
    private final Map<BusinessPriority, Integer> openTicketsByPriority = new HashMap<>();
    private final Map<String, Double> totalImpactByType = new HashMap<>();
    private final Map<String, Double> totalRiskByType = new HashMap<>();
    private final Map<String, AtomicInteger> countByType = new HashMap<>();

    private void processTicket(final Ticket ticket, final String type) {
        if (ticket.getStatus() != Status.CLOSED) {
            totalOpenTickets++;
            openTicketsByType.merge(type, 1, Integer::sum);
            openTicketsByPriority.merge(ticket.getBusinessPriority(), 1, Integer::sum);
            totalImpactByType.merge(type, ticket.calculateImpact(), Double::sum);
            totalRiskByType.merge(type, ticket.calculateRiskScore(), Double::sum);
            countByType.computeIfAbsent(type, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    /**
     * Visits a Bug ticket to collect stability data.
     * @param bug The Bug ticket to visit.
     */
    @Override
    public void visit(final Bug bug) {
        processTicket(bug, "BUG");
    }

    /**
     * Visits a FeatureRequest ticket to collect stability data.
     * @param featureRequest The FeatureRequest ticket to visit.
     */
    @Override
    public void visit(final FeatureRequest featureRequest) {
        processTicket(featureRequest, "FEATURE_REQUEST");
    }

    /**
     * Visits a UiFeedback ticket to collect stability data.
     * @param uiFeedback The UiFeedback ticket to visit.
     */
    @Override
    public void visit(final UiFeedback uiFeedback) {
        processTicket(uiFeedback, "UI_FEEDBACK");
    }

    private String getRiskCategory(final double averageRisk) {
        if (averageRisk >= RISK_CRITICAL_THRESHOLD) {
            return "CRITICAL";
        }
        if (averageRisk >= RISK_SIGNIFICANT_THRESHOLD) {
            return "SIGNIFICANT";
        }
        if (averageRisk >= RISK_MODERATE_THRESHOLD) {
            return "MODERATE";
        }
        return "LOW";
    }

    private String getAppStability(final double totalAverageRisk, final double totalAverageImpact) {
        if (totalAverageRisk > STABILITY_UNSTABLE_THRESHOLD
                || totalAverageImpact > STABILITY_UNSTABLE_THRESHOLD) {
            return "UNSTABLE";
        }
        if (totalAverageRisk > STABILITY_NEEDS_IMPROVEMENT_THRESHOLD
                || totalAverageImpact > STABILITY_NEEDS_IMPROVEMENT_THRESHOLD) {
            return "NEEDS_IMPROVEMENT";
        }
        return "STABLE";
    }

    /**
     * Asambleaza si returneaza tot json node ul
     * @param mapper The ObjectMapper to use for creating nodes.
     * @return The generated report.
     */
    public ObjectNode getReport(final ObjectMapper mapper) {
        ObjectNode report = mapper.createObjectNode();
        report.put("totalOpenTickets", totalOpenTickets);

        ObjectNode ticketsByTypeNode = mapper.createObjectNode();
        openTicketsByType.forEach(ticketsByTypeNode::put);
        report.set("openTicketsByType", ticketsByTypeNode);

        ObjectNode ticketsByPriorityNode = mapper.createObjectNode();
        openTicketsByPriority.forEach((p, c) -> ticketsByPriorityNode.put(p.name(), c));
        report.set("openTicketsByPriority", ticketsByPriorityNode);

        ObjectNode riskByTypeNode = mapper.createObjectNode();
        double totalRiskSum = 0;
        totalRiskByType.forEach((type, riskSum) -> {
            int count = countByType.getOrDefault(type, new AtomicInteger(0)).get();
            double averageRisk = count > 0 ? riskSum / count : 0;
            riskByTypeNode.put(type, getRiskCategory(averageRisk));
        });
        report.set("riskByType", riskByTypeNode);

        ObjectNode impactByTypeNode = mapper.createObjectNode();
        double totalImpactSum = 0;
        totalImpactByType.forEach((type, impactSum) -> {
            int count = countByType.getOrDefault(type, new AtomicInteger(0)).get();
            double averageImpact = count > 0 ? impactSum / count : 0;
            impactByTypeNode.put(type, Math.round(averageImpact * PERCENTAGE_MULTIPLIER)
                    / PERCENTAGE_MULTIPLIER);
        });
        report.set("impactByType", impactByTypeNode);

        for (Double value : totalRiskByType.values()) {
            totalRiskSum += value;
        }
        double avgRisk = totalOpenTickets > 0 ? totalRiskSum / totalOpenTickets : 0;

        for (Double value : totalImpactByType.values()) {
            totalImpactSum += value;
        }
        double avgImpact = totalOpenTickets > 0 ? totalImpactSum / totalOpenTickets : 0;


        report.put("appStability", getAppStability(avgRisk, avgImpact));

        return report;
    }
}
