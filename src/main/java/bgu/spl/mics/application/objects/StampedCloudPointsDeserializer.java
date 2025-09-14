package bgu.spl.mics.application.objects;
import com.google.gson.*;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class StampedCloudPointsDeserializer implements JsonDeserializer<StampedCloudPoints> {
    @Override
    public StampedCloudPoints deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Initialize the object
        StampedCloudPoints stampedCloudPoints = new StampedCloudPoints();
        stampedCloudPoints.setId(jsonObject.get("id").getAsString());
        stampedCloudPoints.setTime(jsonObject.get("time").getAsInt());

        // Deserialize the cloudPoints field
        ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
        JsonArray pointsArray = jsonObject.getAsJsonArray("cloudPoints");

        for (JsonElement pointElement : pointsArray) {
            JsonArray coordinates = pointElement.getAsJsonArray();

            // Extract x and y (ignore z)
            double x = coordinates.get(0).getAsDouble();
            double y = coordinates.get(1).getAsDouble();

            cloudPoints.add(new CloudPoint(x, y));
        }
        stampedCloudPoints.setCloudPoints(cloudPoints);
        return stampedCloudPoints;
    }
}

