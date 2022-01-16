import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import jsonClasses.*;
import java.io.IOException;
import java.util.List;

public class APIParser {
    private ObjectMapper MAPPER = new ObjectMapper();

    private final String[] keys = APIKeys.getKeys();

    public APIParser() throws IOException {
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
        List<Place> allPlaces = MAPPER.readValue(response.body().string(), PlaceList.class).getPlaceList();
        return allPlaces;
    }

    public List<PlaceInRadius> getPlacesInRadius(Position position) throws IOException {
        String response = getResponse("https://api.opentripmap.com/0.1/ru/places/radius?radius=10000&lon=" + position.getLon() + "&lat=" + position.getLat() + "&format=json&limit=10&apikey=" + keys[1]).body().string();
        String cutResponse = "{\" \":" + response + "}";
        return MAPPER.readValue(cutResponse, PlaceListInRadius.class).getPlaceListInRadius();
    }

    public PlaceWithDescription placeDescriptionByXid(String xid) throws IOException {
        String response = getResponse("http://api.opentripmap.com/0.1/ru/places/xid/" + xid + "?apikey=" + keys[1]).body().string();
        return MAPPER.readValue(response, PlaceWithDescription.class);
    }

    public PlaceWithWeather getWeatherByPosition(Position position) throws IOException {
        String response = getResponse("https://api.openweathermap.org/data/2.5/weather?lat=" + position.getLat() + "&lon=" + position.getLon() + "&appid=" + keys[2]).body().string();
        return MAPPER.readValue(response, PlaceWithWeather.class);
    }
}
