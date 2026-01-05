package fileio.UserInput;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class ReporterInput extends UserInput {
}
