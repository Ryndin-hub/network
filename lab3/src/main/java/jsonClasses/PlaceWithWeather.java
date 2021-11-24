package jsonClasses;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class PlaceTemperature {
    private double temp;
    public double getTemp(){
        double temperature = (int) ((temp - 273.15) * 100);
        return temperature / 100.00;
    }
}

@Getter
@Setter
class PlaceWeather {
    private String main;
}

@Getter
@Setter
public class PlaceWithWeather {
    private @JsonProperty("weather")
    List<PlaceWeather> weather;
    private PlaceTemperature main;
    public double getTemp(){
        return main.getTemp();
    }
    public String getWeather(){
        return weather.get(0).getMain();
    }
}
