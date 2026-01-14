package main.Commands;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class TestingPhase {
    private String timestamp;
    private LocalDate testingPhaseStartDate;
    private LocalDate testingPhaseEndDate;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * basic constructor for a testing phase
     */
    public TestingPhase() {
        this.timestamp = null;
        this.testingPhaseStartDate = null;
        this.testingPhaseEndDate = null;
    }

    /**
     *
     * @param timestamp passes the current timestamp and calculates
     *                 the ending date of the testing phase
     */
    public void setTestingPhase(final String timestamp) {
        this.timestamp = timestamp;
        this.testingPhaseStartDate = LocalDate.parse(this.timestamp, formatter);
        this.testingPhaseEndDate = testingPhaseStartDate.plusDays(12);
    }

    /**
     * @param timestamp passes the current tiemstamp and based
     *                  on that it calculat3es rather is is within the testing
     *                  phase or not
     */
    public boolean isInTestingPhase(final String timestamp) {
        if (testingPhaseEndDate == null) {
            return false;
        }
        LocalDate actionDate = LocalDate.parse(timestamp, formatter);
        return !actionDate.isAfter(testingPhaseEndDate);
    }

}
