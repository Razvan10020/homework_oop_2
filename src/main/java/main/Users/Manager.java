package main.Users;

import fileio.UserInputP.ManagerInput;
import fileio.UserInput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class Manager extends User {
        private String hireDate;
        @Getter
        private List<String> subordinates;

        public Manager(final UserInput userInput) {
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
