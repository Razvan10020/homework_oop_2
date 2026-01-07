package main.Users;

import fileio.UserInputP.ManagerInput;
import fileio.UserInput;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Manager extends User{
        private String hireDate;
        private List<String> subordinates;

        public Manager(UserInput userInput) {
            super(userInput.getUsername(),
                    userInput.getEmail(),
                    userInput.getRole());

            ManagerInput managerInput = (ManagerInput) userInput;

            this.hireDate = managerInput.getHireDate();
            this.subordinates = managerInput.getSubordinates() != null
                                ? managerInput.getSubordinates()
                                : new ArrayList<>();
        }
}
