package jsonClasses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceInRadius {
    private String xid;
    private String name;
    private @JsonProperty("point") Position position;
}
