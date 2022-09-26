package com.example.bottomactivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendar;
    TextView date_view;
    Button backButton;
    BottomNavigationView bottomNavigationView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize and assign variable
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        calendar = findViewById(R.id.calendar);
        date_view = findViewById(R.id.date_view);
        backButton = findViewById(R.id.backButton);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.calendar);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {

            // Switch statement for navigating the bottom navigation bar.
            switch (item.getItemId()) {
                case R.id.chooseCity:
                    startActivity(new Intent(getApplicationContext(), CityActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.calendar:
                    return true;
                case R.id.prayerTimes:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });

        calendar
                .setOnDateChangeListener(
                        (view, year, month, dayOfMonth) -> {

                            // Store the value of date with
                            // format in String type Variable
                            // Add 1 in month because month
                            // index is start with 0
                            String date
                                    = dayOfMonth + "-"
                                    + (month + 1) + "-" + year;

                            // set this date in TextView for Display
                            date_view.setText(formatDate(date));
                            MainActivity.setDefaults("chosenDate", date, getApplicationContext());
                        });
        backButton.setOnClickListener(v -> onBackPressed());
    }

    /**
     * @name -
     * @description -
     */
    // Return to MainActivity when back is pressed
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }


    /**
     * TODO - Replace this method with a method using SimpleDateFormat to parse the date!!!
     * @name -
     * @description -
     * @param date - The selected date from Calendar View
     * @return - Returns a formatted version of the Date
     */
    // Function to display date in the wanted format. Should be replaced with code that uses
    // SimpleDateFormat or similar to format the date.
    public String formatDate(String date) {
        String[] arr = date.split("-");

        String month;
        String year = arr[2];

        switch (Integer.parseInt(arr[1])) {
            case 1:
                month = "January";
                break;
            case 2:
                month = "February";
                break;
            case 3:
                month = "March";
                break;
            case 4:
                month = "April";
                break;
            case 5:
                month = "May";
                break;
            case 6:
                month = "June";
                break;
            case 7:
                month = "July";
                break;
            case 8:
                month = "August";
                break;
            case 9:
                month = "September";
                break;
            case 10:
                month = "October";
                break;
            case 11:
                month = "November";
                break;
            case 12:
                month = "December";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + Integer.parseInt(arr[1]));
        }
        return arr[0] + " " + month + " " + year;
    }

}