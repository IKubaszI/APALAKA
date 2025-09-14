package com.example.apalaka;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Calendar;
import com.bumptech.glide.Glide;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.Calendar;
public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "e7b60b26d60a3c6bdb8d707cdc3f8b9b";
    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static final String ICON_URL = "https://openweathermap.org/img/wn/";
    public static final String PREF_FAVORITES = "weather_favorites";
    public static final String PREF_HISTORY = "weather_history";

    private EditText editTextCity;
    private Button buttonGetWeather;
    private Button buttonFavorites;
    private Button buttonHistory;
    private TextView textViewCityName;
    private TextView textViewTemperature;
    private TextView textViewDescription;
    private TextView textViewHumidity;
    private ImageView imageViewWeatherIcon;
    private ImageView imageViewFavorite;

    private WeatherService weatherService;
    private SharedPreferences preferences;
    private AlertDialog historyDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConstraintLayout layout = findViewById(R.id.main_layout);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 16 || hour < 6) {
            layout.setBackgroundResource(R.drawable.weather_bg_night);
        } else {
            layout.setBackgroundResource(R.drawable.weather_bg);
        }

        editTextCity = findViewById(R.id.editTextCity);
        buttonGetWeather = findViewById(R.id.buttonGetWeather);
        buttonFavorites = findViewById(R.id.buttonFavorites);
        buttonHistory = findViewById(R.id.buttonHistory);
        textViewCityName = findViewById(R.id.textViewCityName);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewHumidity = findViewById(R.id.textViewHumidity);
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon);
        imageViewFavorite = findViewById(R.id.imageViewFavorite);

        preferences = getSharedPreferences("weather_prefs", MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = retrofit.create(WeatherService.class);

        buttonGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = editTextCity.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    getCurrentWeatherData(cityName);
                }
            }
        });

        imageViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = textViewCityName.getText().toString();
                if (!cityName.isEmpty()) {
                    toggleFavorite(cityName);
                    updateFavoriteIcon(cityName);
                }
            }
        });

        buttonFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });

        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistoryDialog();
            }
        });
        
    }

    private void getCurrentWeatherData(String cityName) {
        Call<WeatherResponse> call = weatherService.getCurrentWeather(cityName, API_KEY, "metric");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayWeatherData(response.body());
                    addToHistory(response.body().name);
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) { }
        });
    }

    private void displayWeatherData(WeatherResponse weatherResponse) {
        textViewCityName.setText(weatherResponse.name);
        updateFavoriteIcon(weatherResponse.name);

        if (weatherResponse.main != null) {
            int temperature = Math.round(weatherResponse.main.temp);
            textViewTemperature.setText(temperature + "°C");
            textViewHumidity.setText("Wilgotność: " + Math.round(weatherResponse.main.humidity) + "%");
        }

        if (weatherResponse.weather != null && !weatherResponse.weather.isEmpty()) {
            WeatherResponse.Weather weather = weatherResponse.weather.get(0);

            String description = weather.description;
            if (description != null && !description.isEmpty()) {
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
                textViewDescription.setText(description);
            }

            if (weather.icon != null) {
                String iconUrl = ICON_URL + weather.icon + "@2x.png";
                Glide.with(this)
                        .load(iconUrl)
                        .into(imageViewWeatherIcon);
            }
        }
    }

    private void toggleFavorite(String cityName) {
        Set<String> favorites = preferences.getStringSet(PREF_FAVORITES, new HashSet<>());
        Set<String> newFavorites = new HashSet<>(favorites);
        if (favorites.contains(cityName)) {
            newFavorites.remove(cityName);
        } else {
            newFavorites.add(cityName);
        }
        preferences.edit().putStringSet(PREF_FAVORITES, newFavorites).apply();
    }

    private void updateFavoriteIcon(String cityName) {
        Set<String> favorites = preferences.getStringSet(PREF_FAVORITES, new HashSet<>());
        if (favorites.contains(cityName)) {
            imageViewFavorite.setImageResource(R.drawable.ic_star_gold);
        } else {
            imageViewFavorite.setImageResource(R.drawable.ic_star_border);
        }
    }

    private void addToHistory(String cityName) {
        Set<String> historySet = preferences.getStringSet(PREF_HISTORY, new HashSet<>());
        LinkedList<String> history = new LinkedList<>(historySet);
        history.remove(cityName);
        history.addFirst(cityName);
        while (history.size() > 10) history.removeLast();
        preferences.edit().putStringSet(PREF_HISTORY, new HashSet<>(history)).apply();
    }

    private void showHistoryDialog() {
        Set<String> historySet = preferences.getStringSet(PREF_HISTORY, new HashSet<>());
        LinkedList<String> history = new LinkedList<>(historySet);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Historia wyszukiwań");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        for (String city : history) {
            TextView textView = new TextView(this);
            textView.setTextSize(18);
            textView.setText(city);
            textView.setPadding(0, 16, 0, 16);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextCity.setText(city);
                    getCurrentWeatherData(city);
                    if (historyDialog != null && historyDialog.isShowing()) {
                        historyDialog.dismiss();
                    }
                }
            });
            layout.addView(textView);
        }

        builder.setView(layout);
        builder.setNegativeButton("Zamknij", (dialog, which) -> dialog.dismiss());
        historyDialog = builder.create();
        historyDialog.show();
    }
}