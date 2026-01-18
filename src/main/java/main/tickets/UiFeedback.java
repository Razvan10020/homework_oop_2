package main.tickets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.BusinessValue;
import fileio.ActionInput;
import fileio.TicketInput.UiFeedbackInput;
import lombok.Getter;
import main.visitors.TicketVisitor;

@Getter
public class UiFeedback extends Ticket {
    @JsonIgnore
    private final BusinessValue businessValue;
    @JsonIgnore
    private final double usabilityScore;

    public UiFeedback(final int id, final ActionInput actionInput) {
        super(id, actionInput);
        UiFeedbackInput params = (UiFeedbackInput) actionInput.asParams();
        this.businessValue = params.getBusinessValue();
        this.usabilityScore = params.getUsabilityScore();
    }

    /**
     * Accepts a visitor to perform an operation on this UI feedback ticket.
     * @param visitor The visitor.
     */
    @Override
    public void accept(final TicketVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public double calculateImpact() {
        double businessValueNum = getBusinessValue(this.getBusinessValue());

        double baseScore = businessValueNum * this.getUsabilityScore();
        double maxScore = 100.0;

        return Math.min(100.0, (baseScore * 100.0) / maxScore);
    }

    @Override
    public double calculateRiskScore() {
        double businessValueNum = getBusinessValue(this.getBusinessValue());

        double baseScore = (11 - this.getUsabilityScore()) * businessValueNum;
        double maxScore = 100.0;

        return Math.min(100.0, (baseScore * 100.0) / maxScore);
    }

    @Override
    public double calculateEfficiencyScore(long daysToResolve) {
        if (daysToResolve == 0) {
            return 0;
        }
        double businessValueNum = getBusinessValue(this.getBusinessValue());
        
        return (this.getUsabilityScore() + businessValueNum) / daysToResolve;
    }
}
