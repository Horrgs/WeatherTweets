package org.horrgs.weathertweets.wunderground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URLConnection;

/**
 * Created by Horrgs on 3/9/2015.
 */
public class WGLookup implements WGConditions, WGAlert {
    private String key = "";
    private JSONObject jsonObject;
    private JSONArray alertsArray;
    private Protocol protocol;

    public WGLookup(Protocol protocol, String state, String city) {
        setProtocol(protocol);
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("keys.txt"));
            key = bufferedReader.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        WunderGroundAPI wunderGroundAPI = new WunderGroundAPI();
        try {
            this.jsonObject = new JSONObject(getResponse(wunderGroundAPI.openURL(getURL(getProtocol().getProtocolType(), state + "/" + city + ".json"))));
            if(getProtocol() == Protocol.ALERT) {
                this.alertsArray = new JSONArray(jsonObject.getJSONArray("alerts"));
                if(alertsArray.length() == 0) return;
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private String getURL(String protocol, String dir) {
        return WunderGroundAPI.WEBSITE + key + "/" + protocol + "/q/" + dir;
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

    private  <T> T get(String jsonObjectKey, String value) {
        if(getProtocol() != Protocol.ALERT) {
            try {
                return (T) jsonObject.getJSONObject(jsonObjectKey).get(value);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                return (T) jsonObject.getJSONArray(jsonObjectKey).getJSONObject(0).get(value);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    String keyCondition = "current_observation";
    String keyAlert = "alerts";
    String  keyForecast = "";

    /*
    WGCONDITIONS
     */

    @Override
    public double getFTemp() {
        return Double.parseDouble(String.valueOf(get(keyCondition, "temp_f")));
    }

    @Override
    public double getWind() {
        return Double.parseDouble(String.valueOf(get(keyCondition, "wind_mph")));
    }

    @Override
    public double getWindGusts() {
        return Double.valueOf(String.valueOf(get(keyCondition, "wind_gust_mph")));
    }

    @Override
    public String getHumidity() {
        return get(keyCondition, "relative_humidity");
    }

    @Override
    public double getFeelsLike() {
        return Double.valueOf(String.valueOf(get(keyCondition, "feelslike_f")));
    }

    @Override
    public String getForecast() {
        return get(keyCondition, "weather");
    }

    @Override
    public String getPrecipitation() {
        return get(keyCondition, "precip_1hr_in");
    }

    /*
    WGALERT
     */
    @Override
    public String getDescription() {
        return get(keyAlert, "description");
    }

    @Override
    public String dateSet() {
        return get(keyAlert, "date");
    }

    @Override
    public String dateExpires() {
        return get(keyAlert, "expires");
    }

    /*
    WGForecast
     */

    protected Protocol getProtocol() {
        return protocol;
    }

    protected void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    private enum Protocol {
        ALERT("alerts"),
        CONDITION("conditions"),
        FORECAST("forecast");
        public String protocolType;

        private Protocol(String protcolType) { this.protocolType = protcolType; }
        public String getProtocolType() { return protocolType; }
    }
}
