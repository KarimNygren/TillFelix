package com.example.bottomactivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CityActivity extends AppCompatActivity {

    TextView addressText, cityPrayerUpdatedAtText, cityDateText;
    EditText cityEdtText;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        findViewsById();

        final Button searchButton = findViewById(R.id.searchButton);

        bindEnterKeyToSearchButton(searchButton);

        searchButtonClickListener(searchButton);

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.chooseCity);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {

            // Switch statement for navigating the bottom navigation bar.
            switch (item.getItemId()) {
                case R.id.prayerTimes:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.chooseCity:
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
        String homeCity = MainActivity.getDefaults("selectedCity", getApplicationContext());
        String home = homeCity.trim();

        if (!homeCity.isEmpty()) {
            String finalHome = makeFirstLetterUpperCase(home);
            cityEdtText.setText(finalHome);
            addressText.setText(finalHome);
            cityEdtText.setText("");
            setFieldsFromSharedPreferences();
        }

    }

    /**
     * @name - enablePullToRefresh
     * @description -
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    public void enablePullToRefresh() {
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);

        pullToRefresh.setOnRefreshListener(() -> {

            addressText.setText("-");
            cityPrayerUpdatedAtText.setText("-");
            cityDateText.setText("-");

            findViewById(R.id.loadingCircle).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorTxt).setVisibility(View.GONE);

            setFieldsFromSharedPreferences();

            pullToRefresh.setRefreshing(false);
        });
    }

    /**
     * @name - getSearchString
     * @description -
     * @return - The string containing the city that will be used in the API call
     */
    public String getSearchString() {
        EditText search = findViewById(R.id.cityEdtText);
        String city = search.getText().toString();

        String searchCity = "";

        if (!city.matches("")) {
            searchCity = makeFirstLetterUpperCase(city);
            search.setText(searchCity);
            addressText.setText(searchCity);
            cityEdtText.setText("");
        }

        return searchCity;
    }

    /**
     * @name - makeFirstLetterUpperCase
     * @description -
     * @param word - A word string
     * @return - String that needs to have the first letter in Upper Case
     */
    public static String makeFirstLetterUpperCase(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    /**
     * @name - bindEnterKeyToSearchButton
     * @description -
     * @param searchButton - Search button
     */
    //=========== Listener & Mnemonic Methods ===========//
    public void bindEnterKeyToSearchButton(Button searchButton) {

        cityEdtText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                searchButton.performClick();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cityEdtText.getWindowToken(), 0);
                Log.d("SEARCH BUTTON", "API CALL MADE");
            }
            return false;
        });
    }

    /**
     * @name - setFieldsFromSharedPreferences
     * @description -
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setFieldsFromSharedPreferences() {

        String times = MainActivity.getDefaults("prayerTimes", getApplicationContext());

        if (times != null) {


            String[] timesArray = times.split(",");

            String homeCity = MainActivity.getDefaults("selectedCity", getApplicationContext());
            String home = homeCity.trim();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            LocalDateTime now = LocalDateTime.now(ZoneId.of("GMT+2"));

            if (!homeCity.isEmpty()) {
                String finalHome = makeFirstLetterUpperCase(home);
                cityEdtText.setText(finalHome);
                addressText.setText(finalHome);
                cityEdtText.setText("");
                cityPrayerUpdatedAtText.setText(String.format(getString(R.string.updatedAtString), dtf.format(now)));
                cityDateText.setText(timesArray[0]);

                findViewById(R.id.loadingCircle).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * @name - searchButtonClickListener
     * @description -
     * @param searchButton - Sets the button listener for search button
     */
    public void searchButtonClickListener(Button searchButton) {
        searchButton.setOnClickListener(view -> getAndSetLocation());
    }

    /**
     * @name - findViewsById
     * @description -
     *
     */
    public void findViewsById() {
        addressText = findViewById(R.id.address);
        cityPrayerUpdatedAtText = findViewById(R.id.cityPrayerTimesUpdatedAt);
        cityDateText = findViewById(R.id.cityDate);

        cityEdtText = findViewById(R.id.cityEdtText);
    }

    /**
     * @name - getAndSetLocation
     * @description -
     *
     */
    public void getAndSetLocation() {
        String city = getSearchString();

        MainActivity.setDefaults("selectedCity", city, getApplicationContext());

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}