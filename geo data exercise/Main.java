import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")

public class Main {
    final static String outputFilePath = "geo-data.txt";
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void main(String[] args) throws Exception {

        JSONParser jsonParser = new JSONParser();
        String path = "";
        try {
            path = args[0].split("=")[1];
        } catch(Exception e) {
            throw new Exception("path has not been supplied, please use -path:PATH", e);
        }

        Process process = Runtime.getRuntime().exec("java -jar " + path);

        try (InputStream inputStream = process.getInputStream()) {
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            JSONObject jsonObject = (JSONObject)jsonParser.parse(reader);

            JSONArray jsonArray = new JSONArray();
            jsonArray.add(jsonObject);
            List<GeoObject> geoObjectsList = new ArrayList<>();
            //Iterate over json array
            jsonArray.forEach(object -> parseJSONObject(geoObjectsList, (JSONObject) object));

            //convert epoch to Date in geoObjectList
            for (int i = 0; i < geoObjectsList.size(); i++) {
                Date date = new Date((geoObjectsList.get(i).getTimestamp()));
                double mag = Double.valueOf(geoObjectsList.get(i).getMagnitude());
                geoObjectsList.get(i).setMag(mag);
                geoObjectsList.get(i).setDate(date);
            }


            //creates map with highest magnitude for each day
            Map<Date, GeoObject> highestMagnitude = getHighestMagnitude(geoObjectsList);
            Map<Date, Double> averagingMagnitude = getDailyAverage(geoObjectsList);

            printToFileAndConsole(highestMagnitude, averagingMagnitude, outputFilePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private static List<GeoObject> parseJSONObject(List<GeoObject> geoObjectsList, JSONObject JSONObject) {
        //Get features object within list
        JSONArray array = (JSONArray) JSONObject.get("features");
        //add geoObjects to geoObjectsList
        for (int i = 0; i < array.size(); i++) {
            GeoObject geoObject = new GeoObject();
            JSONObject feature = (JSONObject) array.get(i);
            JSONObject properties = (JSONObject) feature.get("properties");
            geoObject.setPlace((String) properties.get("place"));
            geoObject.setTitle((String) properties.get("title"));
            geoObject.setMagnitude(String.valueOf(properties.get("mag")));
            geoObject.setTimestamp((Long) properties.get("time"));

            geoObjectsList.add(geoObject);
        }

        return geoObjectsList;
    }


    private static Map<Date, GeoObject> getHighestMagnitude(List<GeoObject> geoObjectsList) {
        Map<Date, GeoObject> highestMag = geoObjectsList.stream()
                .collect(Collectors.toMap(GeoObject::getDate, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(GeoObject::getMagnitude))));
        return highestMag;
    }

    private static Map<Date, Double> getDailyAverage(List<GeoObject> geoObjectsList)  {
        Map<Date, Double> averages = geoObjectsList.stream()
                .collect(Collectors.groupingBy(GeoObject::getDate,
                        Collectors.averagingDouble(GeoObject::getMag)));
        return averages;
    }


    private static void printToFileAndConsole(Map<Date, GeoObject> highestMag,Map<Date, Double> averageDailyMag, String outputFilePath) {
        File file = new File(outputFilePath);
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(file));

            for (Map.Entry<Date, Double> entry : averageDailyMag.entrySet()) {
                System.out.println(entry.getKey().toString().substring(0, 10) + " Average: " + df.format(entry.getValue()));
                writer.write(entry.getKey().toString().substring(0, 10) + " Average: " + df.format(entry.getValue()));
                writer.newLine();
            }

            for (Map.Entry<Date, GeoObject> entry : highestMag.entrySet()) {
                System.out.println(entry.getValue().getDate().toString().substring(0, 10) + " " + entry.getValue().getMagnitude() + " location: " + entry.getValue().getPlace());
                writer.write(entry.getValue().getDate().toString().substring(0, 10) + " " + entry.getValue().getMagnitude() + " location: " + entry.getValue().getPlace());
                writer.newLine();
            }
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

