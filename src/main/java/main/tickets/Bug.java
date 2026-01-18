package main.tickets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.Frequency;
import enums.Severity;
import fileio.ActionInput;
import fileio.TicketInput.BugInput;
import lombok.Getter;

@Getter
public class Bug extends Ticket {
    @JsonIgnore
    private final Frequency frequency;
    @JsonIgnore
    private final Severity severity;
    @JsonIgnore
    private final String environment;

    public Bug(final int id, final ActionInput actionInput) {
        super(id, actionInput);
        BugInput params = (BugInput) actionInput.asParams();
        this.frequency = params.getFrequency();
        this.severity = params.getSeverity();
        this.environment = params.getEnvironment();
    }

    @Override
    public double calculateImpact() {
        double priorityValue = getBusinessPriorityValue(this.getBusinessPriority());
        double frequencyValue = getFrequencyValue(this.getFrequency());
        double severityValue = getSeverityValue(this.getSeverity());

        double baseScore = priorityValue * frequencyValue * severityValue;
        double maxScore = 48.0; // 4 * 4 * 3

        return Math.min(100.0, (baseScore * 100.0) / maxScore);
    }

    @Override
    public double calculateRiskScore() {
        double frequencyValue = getFrequencyValue(this.getFrequency());
        double severityValue = getSeverityValue(this.getSeverity());

        double baseScore = frequencyValue * severityValue;
        double maxScore = 12.0;

        return Math.min(100.0, (baseScore * 100.0) / maxScore);
    }

    @Override
    public double calculateEfficiencyScore(long daysToResolve) {
        if (daysToResolve == 0) {
            return 0;
        }
        double frequencyValue = getFrequencyValue(this.getFrequency());
        double severityValue = getSeverityValue(this.getSeverity());

        return (frequencyValue + severityValue) * 10.0 / daysToResolve;
    }
}
