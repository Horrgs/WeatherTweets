package org.horrgs.weathertweets.wunderground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URLConnection;
import java.util.Calendar;

/**
 * Created by Horrgs on 3/9/2015.
 */
public class WGLookup implements WGConditions, WGAlert, WGForecast {
    private String key = "";
    private static String message;
    private JSONObject jsonObject;
    private JSONArray alertsArray;
    private Protocol protocol;
    private ForecastType forecastType;

    public WGLookup(Protocol protocol, String state, String city) {
        setProtocol(protocol);
        WunderGroundAPI wunderGroundAPI = new WunderGroundAPI();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("keys.txt"));
            this.jsonObject = new JSONObject(getResponse(wunderGroundAPI.openURL(getURL(getProtocol().getProtocolType(), state + "/" + city + ".json"))));
            if(getProtocol() == Protocol.ALERT) {
                this.alertsArray = new JSONArray(jsonObject.getJSONArray("alerts"));
                if(alertsArray.length() == 0) {
                    setMessage("Zero weather alerts.");
                    return;
                }
            }
            key = bufferedReader.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    private  <T> T get(String jsonObjectKey, String value, String forecastObj) {
        try {
            if(getProtocol() == Protocol.CONDITION) {
                return (T) jsonObject.getJSONObject(jsonObjectKey).get(value);
            } else if(getProtocol() == Protocol.ALERT) {
                return (T) jsonObject.getJSONArray(jsonObjectKey).getJSONObject(0).get(value);
            } else if(getProtocol() == Protocol.FORECAST) {
                if(getForecastType() == ForecastType.TXTFORECAST) {
                    return (T) jsonObject.getJSONObject("forecast").getJSONObject(getForecastType().getForecastType()).getJSONArray("forecastday").get(getPeriod());
                } else if(getForecastType() == ForecastType.SIMPLEFORECAST) {
                    return (T) jsonObject.getJSONObject("forecast").getJSONObject(getForecastType().getForecastType()).getJSONArray("forecastday").getJSONObject(0).getJSONObject(forecastObj);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    String keyCondition = "current_observation";
    String keyAlert = "alerts";
    String  keyForecast = Protocol.FORECAST.getProtocolType();

    /*
    WGCONDITIONS
     */

    @Override
    public double getFTemp() {
        return Double.parseDouble(String.valueOf(get(keyCondition, "temp_f", null)));
    }

    @Override
    public double getWind() {
        return Double.parseDouble(String.valueOf(get(keyCondition, "wind_mph", null)));
    }

    @Override
    public double getWindGusts() {
        return Double.valueOf(String.valueOf(get(keyCondition, "wind_gust_mph", null)));
    }

    @Override
    public String getHumidity() {
        return get(keyCondition, "relative_humidity", null);
    }

    @Override
    public double getFeelsLike() {
        return Double.valueOf(String.valueOf(get(keyCondition, "feelslike_f", null)));
    }

    @Override
    public String getForecast() {
        return get(keyCondition, "weather", null);
    }

    @Override
    public String getPrecipitation() {
        return get(keyCondition, "precip_1hr_in", null);
    }

    /*
    WGALERT
     */
    @Override
    public String getDescription() {
        return get(keyAlert, "description", null);
    }

    @Override
    public String dateSet() {
        return get(keyAlert, "date", null);
    }

    @Override
    public String dateExpires() {
        return get(keyAlert, "expires", null);
    }

    /*
    WGForecast
     */

    /*
    Txt_Forecast
     */
    @Override
    public String getDay() {
        return get(keyForecast, "title", null);
    }

    @Override
    public String getPrediction() {
        return get(keyForecast, "fctext", null);
    }
    @Override
    public double getPrecipitationPossibility() {
        return get(keyForecast, "pop", null);
    }

    /*
    Simpleforecast
     */

    @Override
    public String getAccuDate() {
        return get(keyForecast, "pretty", "date");
    }

    @Override
    public String getAccuHighFahrenheit() {
        return get(keyForecast, "fahrenheit", "high");
    }

    @Override
    public String getAccuLowFahrenheit() {
        return get(keyForecast, "fahrenheit", "low");
    }

    @Override
    public String getAccuConditions() {
        return get(keyForecast, "conditions", null);
    }

    @Override
    public int getAccuPrecipPossibility() {
        return get(keyForecast, "pop", null);
    }

    @Override
    public int getSnowAllDay() {
        return get(keyForecast, "in", "snow_allday");
    }

    @Override
    public int getSnowNight() {
        return get(keyForecast, "in", "snow_night");
    }

    @Override
    public int getMaxWind() {
        return get(keyForecast, "mph", "maxwind");
    }

    @Override
    public int getAvgWind() {
        return get(keyForecast, "mph", "avewind");
    }

    @Override
    public int getAvgHumidity() {
        return get(keyForecast, "avehumidity", null);
    }

    public int getPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendar.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        //If the result is two, one should still be tweeted as it is still the same day.
        hour++;
        return hour <= 12 ? 0 : hour >= 12 && hour <= 18 ? 1 : 2;
    }

    public boolean shouldTweetSimplistic() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(calendar.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour++;
        return hour % 3 == 0 ? true : false;
    }



    protected Protocol getProtocol() {
        return protocol;
    }

    protected void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    protected ForecastType getForecastType() {
        return forecastType;
    }

    protected void setForecastType(ForecastType forecastType) {
        this.forecastType = forecastType;
    }

    public enum Protocol {
        ALERT("alerts"),
        CONDITION("conditions"),
        FORECAST("forecast");
        public String protocolType;

        private Protocol(String protcolType) { this.protocolType = protcolType; }
        public String getProtocolType() { return protocolType; }
    }

    public enum ForecastType {
        TXTFORECAST("txt_forecast"),
        SIMPLEFORECAST("simpleforecast");
        public String forecastType;

        private ForecastType(String forecastType) { this.forecastType = forecastType; }
        public String getForecastType() { return forecastType; }
    }
}
