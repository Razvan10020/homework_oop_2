package main.Users;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import enums.ExpertiseArea;
import enums.Seniority;
import fileio.UserInputP.DeveloperInput;
import fileio.UserInput;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"email", "role"})
public class Developer extends User {
    private String hireDate;
    private ExpertiseArea expertiseArea;
    private Seniority seniority;
    @JsonProperty
    private double performanceScore;

    public Developer(final UserInput userInput) {
        super(userInput.getUsername(),
                userInput.getEmail(),
                userInput.getRole());

        DeveloperInput devInput = (DeveloperInput) userInput;

        this.hireDate = devInput.getHireDate();
        this.expertiseArea = devInput.getExpertiseArea();
        this.seniority = devInput.getSeniority();
    }
}
