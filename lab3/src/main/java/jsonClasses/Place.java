package jsonClasses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Place {
    public String toString(){
        String string = "";
        string += name;
        if (null != country) {
            string += ", " + country;
        }
        if (null != city) {
            string += ", " + city;
        }
        if (null != state) {
            string += ", " + state;
        }
        if (null != street) {
            string += ", " + street;
        }
        if (null != houseNumber) {
            string += ", " + houseNumber;
        }
        return string;
    }
    private String name;
    private String country;
    private String city;
    private String state;
    private String street;
    private String house_number;
    private @JsonProperty("housenumber") String houseNumber;
    private @JsonProperty("point") Position position;
}
