package fileio;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import enums.BusinessValue;
import enums.CustomerDemand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class FeatureRequestInput extends ParamsInput{
    private BusinessValue businessValue;
    private CustomerDemand customerDemand;
}
