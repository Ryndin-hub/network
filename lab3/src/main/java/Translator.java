public class Translator {
    public static String translateWeather(String input){
        input = input.toLowerCase();
        switch (input){
            case "clear":
                return "солнечно";
            case "clouds":
                return "облачно";
            case "rain":
                return "дождь";
            case "mist":
                return "туман";
            case "snow":
                return "снег";
        }
        return input.toLowerCase();
    }
}
