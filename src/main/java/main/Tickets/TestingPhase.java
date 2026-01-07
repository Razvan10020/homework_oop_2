package main.Tickets;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class TestingPhase {
    private String timestamp;
    private LocalDate testingPhaseStartDate;
    private LocalDate testingPhaseEndDate;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public TestingPhase () {
        this.timestamp = null;
        this.testingPhaseStartDate = null;
        this.testingPhaseEndDate = null;
    }

    public void setTestingPhase (String timestamp) {
        this.timestamp = timestamp;
        this.testingPhaseStartDate = LocalDate.parse(this.timestamp, formatter);
        this.testingPhaseEndDate = testingPhaseStartDate.plusDays(12);
    }

    public boolean isInTestingPhase(String timestamp) {
        if (testingPhaseEndDate == null) {
            return false;
        }
        LocalDate actionDate = LocalDate.parse(timestamp, formatter);
        return !actionDate.isAfter(testingPhaseEndDate);
    }

}
