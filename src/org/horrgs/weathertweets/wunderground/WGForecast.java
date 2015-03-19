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

    public double getPrecipitationPossibility();

    /**
     * Use 0 for morning - afternoon. Use 2 for evening. Use 3 for next morning - afternoon. Use 4 for next evening.
     * @return period of the weather you want.
     */
    //TODO: check the current time to determine whether or not to tweet about the morning, or we can use the next morning.
    public int getPeriod();

    /*
    More accurate predictions.
    THESE WILL NEED TO BE ACCESSED THROUGH THE JSONOBJECT "simpleforecast" then through the JSONARRAY "forecastday" then through the next JSONObject
     */

    //PRETTY??
    public String getDate();

    public String getHighFahrenheit();

    public String getLowFahrenheit();
}
