package enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Action {
    ASSIGNED,
    @JsonProperty("DE-ASSIGNED")
    DE_ASSIGNED,
    STATUS_CHANGED,
    ADDED_TO_MILESTONE,
    REMOVED_FROM_DEV
}
