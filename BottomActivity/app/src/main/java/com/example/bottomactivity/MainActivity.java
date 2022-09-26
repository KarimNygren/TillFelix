package com.example.bottomactivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView halfTheNightText, timeToPrayerValueText, dateText, cityOutput, fajrText, sunriseText, dhuhrText, asrText, maghribText, ishaText;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 15 * 1000;

    //=========== Lifecycle Methods ===========//
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Function that only is triggered the first time the app is running.
        // Creates files.
        performTaskOnlyFirstTimeAppRun();

        findViewsById();

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.prayerTimes);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {

            // Switch statement for navigating the bottom navigation bar.
            switch (item.getItemId()) {
                case R.id.chooseCity:
                    startActivity(new Intent(getApplicationContext(), CityActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.prayerTimes:
                    return true;
                case R.id.calendar:
                    startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
        enablePullToRefresh();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();

        String homeCity = getDefaults("selectedCity", getApplicationContext());
        String prayerTimes = getDefaults("prayerTimes", getApplicationContext());
        String halfTheNight = getDefaults("halfTheNight", getApplicationContext());

        // If there's a chosen city, make api call once per day
        if(homeCity != null){
            String home = homeCity.trim();
            cityOutput.setText(home);
            makeApiCallOncePerDay(homeCity);
        }

        // If selected date changes, a new API call needs to be made. Otherwise sets the prayer
        // times from file
        checkIfSelectedDayChanged(prayerTimes, halfTheNight);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        //Start handler as activity become visible
        handler.postDelayed(runnable = () -> {
            calculateAndSetTimeToNextPrayer();
            handler.postDelayed(runnable, delay);
        }, delay);

        super.onResume();
    }

    // If onPause() is not included the threads will double up when i
    // reload the activity
    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable); // Stop handler when activity not visible
        super.onPause();
    }
    //=========== End of Lifecycle Methods ===========//

    /**
     * @name - findViewsById
     * @description - Helper method to find views by ids - Purpose is a cleaner onCreate()
     */
    private void findViewsById() {
        timeToPrayerValueText = findViewById(R.id.nextPrayerValue);
        dateText = findViewById(R.id.date);
        cityOutput = findViewById(R.id.cityOutput);
        fajrText = findViewById(R.id.fajr);
        sunriseText = findViewById(R.id.sunrise);
        dhuhrText = findViewById(R.id.dhuhr);
        asrText = findViewById(R.id.asr);
        maghribText = findViewById(R.id.maghrib);
        ishaText = findViewById(R.id.isha);
        halfTheNightText = findViewById(R.id.halfnight);
    }

    /**
     * @name - calculateAndSetTimeToNextPrayer
     * @description - Calculates the time to the next prayer closest in time. Sets the text field
     *                for NextPrayer with the value.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void calculateAndSetTimeToNextPrayer() {

        LocalTime fajrTime = LocalTime.parse(fajrText.getText().subSequence(0, 5));
        LocalTime dhuhrTime = LocalTime.parse(dhuhrText.getText().subSequence(0, 5));
        LocalTime asrTime = LocalTime.parse(asrText.getText().subSequence(0, 5));
        LocalTime maghribTime = LocalTime.parse(maghribText.getText().subSequence(0, 5));
        LocalTime ishaTime = LocalTime.parse(ishaText.getText().subSequence(0, 5));

        LocalTime sweTime = LocalTime.now(ZoneId.of("GMT+2"));

        LocalTime thePrayer = null;

        if (sweTime.isBefore(fajrTime) && !sweTime.isAfter(fajrTime)) {
            thePrayer = fajrTime;
        } else if (sweTime.isBefore(dhuhrTime) && !sweTime.isAfter(dhuhrTime)) {
            thePrayer = dhuhrTime;
        } else if (sweTime.isBefore(asrTime) && !sweTime.isAfter(asrTime)) {
            thePrayer = asrTime;
        } else if (sweTime.isBefore(maghribTime) && !sweTime.isAfter(maghribTime)) {
            thePrayer = maghribTime;
        } else if (sweTime.isBefore(ishaTime) && !sweTime.isAfter(ishaTime)) {
            thePrayer = ishaTime;
        } else if (sweTime.isAfter(ishaTime)) {
            thePrayer = fajrTime;
        }

        if (thePrayer == null) throw new AssertionError();

        LocalTime theHour = thePrayer.minusHours(sweTime.getHour());
        LocalTime timeToPrayer = theHour.minusMinutes(sweTime.getMinute());

        timeToPrayerValueText.setText(String.valueOf(timeToPrayer));
    }

    /**
     * @name - checkIfSelectedDayChanged
     * @description - Checks if a new date has been selected from Calendar View, if it has a new
     *                PrayerTask(AsyncTask) is triggered. Otherwise it sets the prayer times from
     *                data stored in SharedPreferences from the daily API call.
     * @param prayerTimes - String with prayer times from SharedPreferences
     * @param halfTheNight - Time for when half of the night occurs from SharedPreferences
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkIfSelectedDayChanged(String prayerTimes, String halfTheNight){
        String sharedPrefDate = getDefaults("chosenDate", getApplicationContext());

        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH + 1) + "-" + calendar.get(Calendar.YEAR);

        if (!Objects.equals(sharedPrefDate, date)) {
            new PrayerTask().execute();
        } else{
            setTimesFromStoredData(prayerTimes, halfTheNight);
        }
    }

    /**
     * @name - calculateHalfNight
     * @description - Calculates at which time half of the night has passed, the Isha prayer has to
     *                be performed before this time, why it is relevant to calculate it.
     * @param sunsetToday - The sunset time of the selected date
     * @param fajr - The Fajr prayer time from the day after selected date
     * @return - Returns the time where half of the night has passed
     * @throws ParseException - ParseException thrown due to LocalTime parsing
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String calculateHalfNight(String sunsetToday, String fajr) throws ParseException {
        LocalTime sun = LocalTime.parse(sunsetToday);
        LocalTime faj = LocalTime.parse(fajr);

        long sunHour = sun.getHour();
        long sunMin = sun.getMinute();
        long fajHour = faj.getHour();
        long fajMin = faj.getMinute();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date startDate = simpleDateFormat.parse(sunHour + ":" + sunMin);
        Date endDate = simpleDateFormat.parse(fajHour + ":" + fajMin);

        assert endDate != null;
        assert startDate != null;

        long difference = endDate.getTime() - startDate.getTime();

        if (difference < 0) {
            Date dateMax = simpleDateFormat.parse("24:00");
            Date dateMin = simpleDateFormat.parse("00:00");
            assert dateMax != null;
            assert dateMin != null;
            difference = dateMax.getTime() - startDate.getTime() + endDate.getTime() - dateMin.getTime();
        }

        int days = (int) (difference / (1000 * 60 * 60 * 24));
        int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
        Log.d("log_tag", "Hours: " + hours + ", Minutes: " + min);

        int totalMinutes = min + (hours * 60);

        LocalTime halfOfTheNight = sun.plusMinutes(totalMinutes / 2);

        return halfOfTheNight.toString();
    }


    /**
     * @name - getJSONandSetFields
     * @description - Get's the result of the API call and calls several methods that handles the
     *                JSON data. If JSON operations fails, an error screen will appear and the
     *                Handler with postDelayed causes the Runnable to wait 5 seconds and then
     *                the Intent will trigger the MainActivity again.
     *
     * @param result - The result of the API Call
     */

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getJSONandSetFields(String result) {

        // Gets the stored city that is set.
        String city = getSelectedCity();

        // Gets the index of selected day in calendar and also gets the index for the following
        // day in order to get the following days fajr prayer time to calculate half of the night.
        int finalDay = getWantedDay() - 1;
        int dayAfterFinalDay = getWantedDay();

        try {
            // Creates the JSON Object and returns an array with JSON Data
            JSONArray data = createJSONObject(result);

            /*
             Extracts the date and prayer times from the JSON Array and returns the values in
             a String Array.
            */
            String[] prayerTimeData = extractValuesFromJSON(data, finalDay);

            /*
               Get's tomorrows Fajr time from the JSON Array and calls calculateHalfNight() method.
               Sets the half night text value and saves that value to a text file.
            */
            int lastDayOfCurrentMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

            if(dayAfterFinalDay != lastDayOfCurrentMonth){
                getTomorrowFajrAndSetHalfNightValue(data, prayerTimeData[5], dayAfterFinalDay);
            }else{
                getTomorrowFajrAndSetHalfNightValue(data, prayerTimeData[5], finalDay);
            }


            /* Sets the prayer time fields with data from the JSON Array and writes the selected city
               to a file.
               Calls formatTimeStringAndSaveToFile() method that formats a String with the prayer
               times and saves the times to a file.
             */
            setFieldsFromJsonAndSaveSelectedCity(prayerTimeData, city);

            findViewById(R.id.loadingCircle).setVisibility(View.GONE);
            findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            findViewById(R.id.loadingCircle).setVisibility(View.GONE);
            findViewById(R.id.errorTxt).setVisibility(View.VISIBLE);

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }, 5000);
        }
    }

    /**
     * @name - createJSONObject
     * @description - Creates a JSON Object and then extracts the "data" to a JSON Array
     * @param result - The result of the API Call
     * @return - Returns a JSON Array extracted from the JSON Object
     * @throws JSONException - Throws JSONException due to use of JSON methods
     */
    private JSONArray createJSONObject(String result) throws JSONException {
        JSONObject jsonObj = new JSONObject(result);

        return jsonObj.getJSONArray("data");
    }

    /**
     * @name - extractValuesFromJSON
     * @description - Extracts the values from the JSON Array and get the prayer times and date from
     *                the Array. Returns a new String Array with the relevant values.
     *
     * @param data - JSONArray containing all API data
     * @param finalDay - The selected date in Calendar View
     * @return - Returns a String Array with the date and prayer times
     * @throws JSONException - Throws JSONException due to use of JSON methods
     */
    private String[] extractValuesFromJSON(JSONArray data, int finalDay) throws JSONException {

        JSONObject wantedDayArr = data.getJSONObject(finalDay);
        JSONObject timings = wantedDayArr.getJSONObject("timings");
        JSONObject date = wantedDayArr.getJSONObject("date");

        String todayDate = date.getString("readable");
        String fajr = timings.getString("Fajr");
        String sunrise = timings.getString("Sunrise");
        String dhuhr = timings.getString("Dhuhr");
        String asr = timings.getString("Asr");
        String maghrib = timings.getString("Maghrib");
        String isha = timings.getString("Isha");

        return new String[]{todayDate, fajr, sunrise, dhuhr, asr, maghrib, isha};
    }

    /**
     * @name - setFieldsFromJsonAndSaveSelectedCity
     * @description - Sets the relevant text fields from the String Array of values. Calls a method
     *                that will format the values to a String and store it in SharedPreferences.
     *
     * @param timeArr - Array with the prayer times
     * @param city - The city that the prayer times is for
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setFieldsFromJsonAndSaveSelectedCity(String[] timeArr, String city) {

        fajrText.setText(timeArr[1].substring(0, 5));
        sunriseText.setText(timeArr[2].substring(0, 5));
        dhuhrText.setText(timeArr[3].substring(0, 5));
        asrText.setText(timeArr[4].substring(0, 5));
        maghribText.setText(timeArr[5].substring(0, 5));
        ishaText.setText(timeArr[6].substring(0, 5));
        dateText.setText(timeArr[0]);
        cityOutput.setText(CityActivity.makeFirstLetterUpperCase(city));

        formatTimeStringAndSaveToFile(timeArr);
    }

    /**
     * @name - formatTimeStringAndSaveToFile
     * @description - Formats the values to a String and stores it in SharedPreferences. Calls a
     *                method that calculates the time to the next prayer closest in time and sets
     *                the NextPrayer text field with the resulting value.
     *
     * @param timeArr - String Array with prayer times
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void formatTimeStringAndSaveToFile(String[] timeArr) {

        String times = timeArr[0] + "," +
                timeArr[1].substring(0, 5) + "," +
                timeArr[2].substring(0, 5) + "," +
                timeArr[3].substring(0, 5) + "," +
                timeArr[4].substring(0, 5) + "," +
                timeArr[5].substring(0, 5) + "," +
                timeArr[6].substring(0, 5);

        setDefaults("prayerTimes", times, getApplicationContext());

        calculateAndSetTimeToNextPrayer();
    }

    /**
     * @name - getWantedDay
     * @description - Get's the selected date from Calendar View and extracts the number holding the
     *                day of the month in the Date String.
     *
     * @return The selected day from Calendar View.
     */
    private int getWantedDay() {
        // Gets the current day of the month
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int wantedDay;

        // Get's the chosen date selected from Calendar view
        String sharedPrefDate = getDefaults("chosenDate", getApplicationContext());

        // If the shared preferences date is not null, gets the date and splits it to array,
        // in order to get the day of the month chosen so that we can extract the correct values
        // from the JSON object.
        if (sharedPrefDate != null) {
            String[] wantedDateArr = sharedPrefDate.split("-");
            wantedDay = Integer.parseInt(wantedDateArr[0]);
        } else {
            wantedDay = currentDay;
        }


        return wantedDay;
    }

    /**
     * @name getSelectedCity
     *
     * @description Gets the selected city from the city text field.
     *
     * @return The selected city from city output text view.
     *
     * @return_type String
     */
    private String getSelectedCity() {
        return cityOutput.getText().toString().toLowerCase();
    }

    /**
     * @name - getTomorrowFajrAndSetHalfNightValue
     *
     * @description - Gets the value of the Fajr prayer from the day after the selected day. Then
     *                uses that value and the selected day's sunset value to calculate the value
     *                that represents half of the night
     *
     * @param prayerTimeData - JSONArray containing prayer time data
     * @param maghrib        - Maghrib prayer time string
     * @param dayAfterWanted - The day after the selected day in Calendar View
     *
     * @throws ParseException - ParseException due to parsing performed in calculateHalfNight method
     * @throws JSONException  - JSONException due to getJSONObject method call
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getTomorrowFajrAndSetHalfNightValue(JSONArray prayerTimeData, String maghrib, int dayAfterWanted) throws JSONException, ParseException {
        // Get tomorrows Fajr and calculate & set the half of the night
        JSONObject nextDayArr = prayerTimeData.getJSONObject(dayAfterWanted);
        JSONObject tomorrowTimings = nextDayArr.getJSONObject("timings");
        String fajrTomorrow = tomorrowTimings.getString("Fajr");
        String trimmedFajr = fajrTomorrow.substring(0, 5);

        String sunsetToday = maghrib.substring(0, 5);
        String halfOfNight = calculateHalfNight(sunsetToday, trimmedFajr);
        halfTheNightText.setText(halfOfNight);

        setDefaults("halfTheNight", halfOfNight, getApplicationContext());
    }

    /**
     * @name - makeApiCallOncePerDay
     *
     * @description - Check if a it's a new day, if that is the case it will perform one api call.
     *                If it's not a new day, the prayer times are set from data stored in a file.
     *
     * @param homeCity - The selected city is passed to the method in order to check that it's not
     *                   empty before making the API call.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void makeApiCallOncePerDay(String homeCity) {

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        int lastDay = settings.getInt("day", 0);

        String prayerTimes = getDefaults("prayerTimes", getApplicationContext());

        String halfTheNight = getDefaults("halfTheNight", getApplicationContext());

        // Checks if there's a new day.
        if (lastDay != currentDay) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("day", currentDay);
            editor.apply();

            // If there's a chosen city, perform the API call of the day.
            if (!homeCity.isEmpty()) {
                new PrayerTask().execute();
            }
        } else {
            // If there's not a new day, the text views values are set from stored data
            setTimesFromStoredData(prayerTimes, halfTheNight);
        }
    }


    /**
     * @name - enablePullToRefresh
     * @description - Enables the Pull To Refresh functionality. When the user pulls down to refresh
     *                the page resets all the values in the text views and then performs a new
     *                PrayerTask(AsyncTask), triggering a new API call which in turn will fill the
     *                text views with the relevant values.
     */
    @SuppressLint("SetTextI18n")
    private void enablePullToRefresh() {
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);

        pullToRefresh.setOnRefreshListener(() -> {

            dateText.setText("-");
            fajrText.setText("-");
            sunriseText.setText("-");
            dhuhrText.setText("-");
            asrText.setText("-");
            maghribText.setText("-");
            ishaText.setText("-");

            new PrayerTask().execute();
            pullToRefresh.setRefreshing(false);
        });
    }


    /**
     * @name - getApiUrl
     * @description - Get's the selected city from SharedPreferences and makes the String lower case.
     *                If the selected city is empty, a Toast message will be sent by using runOnUiThread
     *                in order for the Toast Message to be run on the UI thread.
     * @return - Returns the URL to be used in the API Call.
     */
    private String getApiUrl() {

        String city = getDefaults("selectedCity", getApplicationContext());

        String CITY_COUNTRY;

        if(!city.isEmpty()){
            city = city.toLowerCase();
        }

        if (city.matches("")) {
            runOnUiThread(() -> {
                Toast toast = Toast.makeText(getApplicationContext(), "Your input cannot be empty, enter a city name.", Toast.LENGTH_SHORT);
                toast.show();
            });
        }
        return "http://api.aladhan.com/v1/calendarByCity?city=" + city + "&country=*";
    }

    /**
     * @name - performTaskOnlyFirstTimeAppRun
     * @description - This method is only performed the first time the app is running.
     *                It calls initialize SharedPreferences for selected City.
     *
     *
     */
    private void performTaskOnlyFirstTimeAppRun() {
        final String PREFS_NAME = "MyPrefsFile";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        // Using SharedPreferences to check if it's the first time the app is running.
        // If that is the case, initialize SharedPrefs and then set the first time boolean value to false.
        if (settings.getBoolean("my_first_time", true)) {

            //the app is being launched for first time, call createFilesIfDoNotExist() method.
            initializeSharedPreferences();

            // Record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply();
        }
    }

    /**
     * @name  - setTimesFromStoredData
     * @description - Sets all values with stored data from SharedPreferences and performs method
     *                call that calculates the time to the next prayer.
     * @param prayerTimes - A formatted String with the prayer times
     * @param halfTheNight - Time where half of the night has passed
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setTimesFromStoredData(String prayerTimes, String halfTheNight) {

        if(prayerTimes != null){
            String[] timesArray = prayerTimes.split(",");

            dateText.setText(timesArray[0]);
            fajrText.setText(timesArray[1]);
            sunriseText.setText(timesArray[2]);
            dhuhrText.setText(timesArray[3]);
            asrText.setText(timesArray[4]);
            maghribText.setText(timesArray[5]);
            ishaText.setText(timesArray[6]);
            halfTheNightText.setText(halfTheNight);

            calculateAndSetTimeToNextPrayer();
        }
    }


    /**
     * @name - initializeSharedPreferences
     * @description - If it's the first time running the app and there is no value for selected city
     *                in SharedPrefs, set Kista as the default city until another city is chosen.
     */
    private void initializeSharedPreferences() {
        if (getDefaults("selectedCity", getApplicationContext()) == null) {
            setDefaults("selectedCity", "Kista", getApplicationContext());
        }
    }

    /**
     * @name - setDefaults
     * @description - Sets the values in SharedPreferences - Static so that it can be called from
     *                other Activities.
     * @param key - The key for the stored data
     * @param value - The value to be stored
     * @param context - The application context
     */
    @SuppressLint("ApplySharedPref")
    protected static void setDefaults(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit(); // or editor.apply() in case you don't want to write data instantly
    }

    /**
     * @name - getDefaults
     * @description - Gets the values from SharedPreferences - Static so that it can be called from
     *                other Activities.
     * @param key - The key for the stored data
     * @param context - The application context
     * @return - Returns the string with data
     */
    protected static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    //=========== PrayerTask - AsyncTask ===========//
    @SuppressWarnings("deprecation")
    @SuppressLint({"StaticFieldLeak", "NewApi"})
    class PrayerTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.loadingCircle).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorTxt).setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = getApiUrl();

            return HttpGetRequest.executeHttpGetRequest(URL);
        }

        @Override
        protected void onPostExecute(String today) {
            getJSONandSetFields(today);
        }
    }
    //=========== End of PrayerTask - AsyncTask ===========//

}