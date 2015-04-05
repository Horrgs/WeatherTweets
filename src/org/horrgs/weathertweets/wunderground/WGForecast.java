package org.horrgs.weathertweets.wunderground;

/**
 * Created by Horrgs on 3/18/2015.
 */
public interface WGForecast {
    /*
    MORNING AND NIGHT TWEETS
     */
    public String getDay();

    public String getPrediction();

    public String getPrecipitationPossibility();

    /**
     * Use 0 for morning - afternoon. Use 1 for evening. Use 2 for next morning - afternoon. Use 3 for next evening.
     * @return period of the weather you want.
     */
    //TODO: check the current time to determine whether or not to tweet about the morning, or we can use the next morning.
    public int getPeriod();

    /*
    More accurate predictions.
    THESE WILL NEED TO BE ACCESSED THROUGH THE JSONOBJECT "simpleforecast" then through the JSONARRAY "forecastday" then through the next JSONObject
     */

    //PRETTY??
    public String getAccuDate();

    public String getAccuHighFahrenheit();

    public String getAccuLowFahrenheit();

    public String getAccuConditions();

    public int getAccuPrecipPossibility();

    public int getSnowAllDay();

    public int getSnowNight();

    public int getMaxWind();

    public int getAvgWind();

    public int getAvgHumidity();
}
