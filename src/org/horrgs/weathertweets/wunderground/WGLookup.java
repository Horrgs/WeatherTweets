package org.horrgs.weathertweets.wunderground;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URLConnection;
import java.nio.Buffer;

/**
 * Created by Horrgs on 3/9/2015.
 */
public class WGLookup implements WGMainFrame {
    private String state = "";
    private String city = "";
    private String key = "";
    private JSONObject jsonObject;
    public WGLookup(String state, String city) {
        this.state = state;
        this.city = city;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("keys.txt"));
            key = bufferedReader.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //WunderGroundAPI.WEBSITE + key + "/conditions/q/";
        WunderGroundAPI wunderGroundAPI = new WunderGroundAPI();
        try {
            this.jsonObject = new JSONObject(getResponse(wunderGroundAPI.openURL(getURL(state + "/" + city + ".json"))));
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private String getURL(String dir) {
        return WunderGroundAPI.WEBSITE + key + "/conditions/q/" + dir;
    }

    private String getResponse(URLConnection urlConnection) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line= bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private  <T> T get(String value) {
        try {
            return (T) jsonObject.getJSONObject("current_observation").get(value);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public double getFTemp() {
        return Double.parseDouble(String.valueOf(get("temp_f")));
    }

    @Override
    public double getWind() {
        return Double.parseDouble(String.valueOf(get("wind_mph")));
    }

    @Override
    public double getWindGusts() {
        return Double.valueOf(String.valueOf(get("wind_gust_mph")));
    }

    @Override
    public String getHumidity() {
        return get("relative_humidity");
    }

    @Override
    public double getFeelsLike() {
        return Double.valueOf(String.valueOf(get("feelslike_f")));
    }

    @Override
    public String getForecast() {
        return get("weather");
    }

    @Override
    public String getPrecipitation() {
        return get("precip_1hr_in");
    }
}
