package main.Users;

import enums.ExpertiseArea;
import enums.Seniority;
import fileio.UserInputP.DeveloperInput;
import fileio.UserInput;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Developer extends User {
    private String hireDate;
    private ExpertiseArea expertiseArea;
    private Seniority seniority;

    public Developer(UserInput userInput) {
        super(userInput.getUsername(),
                userInput.getEmail(),
                userInput.getRole());

        DeveloperInput devInput = (DeveloperInput) userInput;

        this.hireDate = devInput.getHireDate();
        this.expertiseArea = devInput.getExpertiseArea();
        this.seniority = devInput.getSeniority();
    }
}
