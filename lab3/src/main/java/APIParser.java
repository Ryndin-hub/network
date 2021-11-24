import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import jsonClasses.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class APIParser {
    ObjectMapper MAPPER = new ObjectMapper();

    private final String[] keys = {
            "e4279841-12d2-4a89-bd3b-5a7bcd545b52",
            "5ae2e3f221c38a28845f05b67910dcd1869ac3c2b8e2f9adb3350a08",
            "e316ec26153756c6cbe2c151bb7bc827"
    };

    public APIParser(){
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private Response getResponse(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return client.newCall(request).execute();
    }

    public List<Place> getPlacesByName(String name) throws IOException {
        Response response = getResponse("https://graphhopper.com/api/1/geocode?q=" + name + "&limit=10&key=" + keys[0]);
        List<Place> ruPlaces = MAPPER.readValue(response.body().string(), PlaceList.class).getPlaceList();
        response = getResponse("https://graphhopper.com/api/1/geocode?q=" + name + "&limit=10&locale=en&key=" + keys[0]);
        List<Place> enPlaces = MAPPER.readValue(response.body().string(), PlaceList.class).getPlaceList();
        if (null == ruPlaces && null == enPlaces){
            return Collections.emptyList();
        }
        if (null == ruPlaces){
            return enPlaces;
        }
        if (null == enPlaces){
            return ruPlaces;
        }
        List<Place> allPlaces = ruPlaces;
        for (Place place : enPlaces){
            boolean flag = false;
            for (Place placeRu : ruPlaces){
                if (place.getPosition().getLon() == placeRu.getPosition().getLon() && place.getPosition().getLat() == placeRu.getPosition().getLat()){
                    flag = true;
                }
            }
            if (!flag) allPlaces.add(place);
        }
        return allPlaces;
    }

    public List<PlaceInRadius> getPlacesInRadius(Position position) throws IOException {
        Response response = getResponse("https://api.opentripmap.com/0.1/ru/places/radius?radius=10000&lon=" + position.getLon() + "&lat=" + position.getLat() + "&format=json&limit=10&apikey=" + keys[1]);
        String cutResponse = "{\" \":" + response.body().string() + "}";
        return MAPPER.readValue(cutResponse, PlaceListInRadius.class).getPlaceListInRadius();
    }

    public PlaceWithDescription placeDescriptionByXid(String xid) throws IOException {
        Response response = getResponse("http://api.opentripmap.com/0.1/ru/places/xid/" + xid + "?apikey=" + keys[1]);
        return MAPPER.readValue(response.body().string(), PlaceWithDescription.class);
    }

    public PlaceWithWeather getWeatherByPosition(Position position) throws IOException {
        Response response = getResponse("https://api.openweathermap.org/data/2.5/weather?lat=" + position.getLat() + "&lon=" + position.getLon() + "&appid=" + keys[2]);
        return MAPPER.readValue(response.body().string(), PlaceWithWeather.class);
    }
}
