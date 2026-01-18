package main.visitors;

import main.tickets.Bug;
import main.tickets.FeatureRequest;
import main.tickets.UiFeedback;

/**
 * Defines the Visitor interface for operating on different types of Ticket objects.
 */
public interface TicketVisitor {
    /**
     * Visits a Bug ticket.
     * @param bug The Bug ticket to be visited.
     */
    void visit(Bug bug);

    /**
     * Visits a FeatureRequest ticket.
     * @param featureRequest The FeatureRequest ticket to be visited.
     */
    void visit(FeatureRequest featureRequest);

    /**
     * Visits a UiFeedback ticket.
     * @param uiFeedback The UiFeedback ticket to be visited.
     */
    void visit(UiFeedback uiFeedback);
}
