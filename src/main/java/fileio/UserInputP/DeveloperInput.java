package fileio.UserInputP;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import enums.ExpertiseArea;
import enums.Seniority;
import fileio.UserInput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class DeveloperInput extends UserInput {
    private String hireDate;
    private ExpertiseArea expertiseArea;
    private Seniority seniority;
}
