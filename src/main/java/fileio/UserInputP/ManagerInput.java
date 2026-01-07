package fileio.UserInputP;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.UserInput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class ManagerInput extends UserInput {
    private String hireDate;
    private List<String> subordinates;
}
