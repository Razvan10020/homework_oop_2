package fileio.UserInputP;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import fileio.UserInput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ReporterInput extends UserInput {
}
