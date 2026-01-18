package main.tickets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.BusinessValue;
import enums.CustomerDemand;
import fileio.ActionInput;
import fileio.TicketInput.FeatureRequestInput;
import lombok.Getter;

@Getter
public class FeatureRequest extends Ticket {
    @JsonIgnore
    private final BusinessValue businessValue;
    @JsonIgnore
    private final CustomerDemand customerDemand;

    public FeatureRequest(final int id, final ActionInput actionInput) {
        super(id, actionInput);
        FeatureRequestInput params = (FeatureRequestInput) actionInput.asParams();
        this.businessValue = params.getBusinessValue();
        this.customerDemand = params.getCustomerDemand();
    }

    @Override
    public double calculateImpact() {
        double businessValueNum = getBusinessValue(this.getBusinessValue());
        double customerDemandNum = getCustomerDemandValue(this.getCustomerDemand());

        double baseScore = businessValueNum * customerDemandNum;
        double maxScore = 100.0;

        return Math.min(100.0, (baseScore * 100.0) / maxScore);
    }

    @Override
    public double calculateRiskScore() {
        double businessValueNum = getBusinessValue(this.getBusinessValue());
        double customerDemandNum = getCustomerDemandValue(this.getCustomerDemand());

        double baseScore = businessValueNum + customerDemandNum;
        double maxScore = 20.0;

        return Math.min(100.0, (baseScore * 100.0) / maxScore);
    }

    @Override
    public double calculateEfficiencyScore(long daysToResolve) {
        if (daysToResolve == 0) {
            return 0;
        }
        double businessValueNum = getBusinessValue(this.getBusinessValue());
        double customerDemandNum = getCustomerDemandValue(this.getCustomerDemand());

        return (businessValueNum + customerDemandNum) / daysToResolve;
    }
}
