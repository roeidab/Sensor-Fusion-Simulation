package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static class LiDarDataBaseHolder{
        private static final LiDarDataBase instance = new LiDarDataBase(DataPath);
    }

    private static String DataPath;
    private ArrayList<StampedCloudPoints> cloudPoints = null; // The coordinates of what we have for every object per time

    private LiDarDataBase(String lidarDataJsonPath)
    {
        loadLiDarData(lidarDataJsonPath);
    }


    public static LiDarDataBase getInstance() {
        if (DataPath != null )
        {
            return LiDarDataBaseHolder.instance;
        }
        return null; // Can't return instance since there's no path to Database yet
    }

    /**
     * Returns the singleton instance of LiDarDataBase.
     * Will be called from main with the path to the json file
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */

    public static LiDarDataBase getInstance(String filePath) {
        DataPath = filePath;
        return LiDarDataBaseHolder.instance;

    }

    /**
     * Load LiDAR data from the specified file.
     *
     * @param lidarDataJsonPath The path to the LiDAR data file.
     */
    private void loadLiDarData(String lidarDataJsonPath) {
        try (FileReader reader = new FileReader(lidarDataJsonPath)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(StampedCloudPoints.class, new StampedCloudPointsDeserializer())
                    .create();

            // Define the type for List<StampedCloudPoints>
            Type listType = new TypeToken<List<StampedCloudPoints>>() {
            }.getType();

            // Deserialize the JSON file
            cloudPoints = gson.fromJson(reader, listType);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load LiDAR data from " + lidarDataJsonPath, e);
        }
    }

    public int lastObjTime()
    {
        if (cloudPoints != null )
        {
            return cloudPoints.get(cloudPoints.size()-1).getTime();
        }
        return -1;
    }


    public ArrayList<StampedCloudPoints> getCloudPoints()
    {
        return cloudPoints;
    }

}
