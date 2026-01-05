package fileio.UserInput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
