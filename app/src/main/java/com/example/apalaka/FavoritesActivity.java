package com.example.apalaka;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FavoritesActivity extends AppCompatActivity {

    private LinearLayout favoritesContainer;
    private SharedPreferences preferences;
    private WeatherService weatherService;
    private static final String ICON_URL = "https://openweathermap.org/img/wn/";
    private static final String API_KEY = "e7b60b26d60a3c6bdb8d707cdc3f8b9b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesContainer = findViewById(R.id.favoritesContainer);
        preferences = getSharedPreferences("weather_prefs", MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = retrofit.create(WeatherService.class);

        Set<String> favoritesSet = preferences.getStringSet(MainActivity.PREF_FAVORITES, new HashSet<>());
        LinkedList<String> favorites = new LinkedList<>(favoritesSet);

        for (String city : favorites) {
            addCityWeatherView(city);
        }
    }

    private void addCityWeatherView(final String cityName) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        itemLayout.setPadding(0, 48, 0, 48);
        itemLayout.setBackgroundResource(R.drawable.favorite_item_bg);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(140, 140);
        iconParams.setMargins(32, 0, 32, 0);
        icon.setLayoutParams(iconParams);

        TextView cityText = new TextView(this);
        cityText.setTextSize(22);
        cityText.setText(cityName);
        cityText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        LinearLayout.LayoutParams cityParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f);
        cityText.setLayoutParams(cityParams);

        TextView tempText = new TextView(this);
        tempText.setTextSize(24);
        tempText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        LinearLayout.LayoutParams tempParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        tempText.setLayoutParams(tempParams);

        ImageView star = new ImageView(this);
        LinearLayout.LayoutParams starParams = new LinearLayout.LayoutParams(100, 100);
        starParams.setMargins(32, 0, 32, 0);
        star.setLayoutParams(starParams);
        star.setImageResource(R.drawable.ic_star_gold);

        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromFavorites(cityName);
                favoritesContainer.removeView(itemLayout);
            }
        });

        itemLayout.addView(icon);
        itemLayout.addView(cityText);
        itemLayout.addView(tempText);
        itemLayout.addView(star);

        favoritesContainer.addView(itemLayout);

        Call<WeatherResponse> call = weatherService.getCurrentWeather(cityName, API_KEY, "metric");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    if (weather.weather != null && !weather.weather.isEmpty()) {
                        String iconId = weather.weather.get(0).icon;
                        Glide.with(FavoritesActivity.this)
                                .load(ICON_URL + iconId + "@2x.png")
                                .into(icon);
                    }
                    if (weather.main != null) {
                        int temp = Math.round(weather.main.temp);
                        tempText.setText(temp + "°C");
                    }
                } else {
                    tempText.setText("--");
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tempText.setText("--");
            }
        });
    }

    private void removeFromFavorites(String cityName) {
        Set<String> favorites = preferences.getStringSet(MainActivity.PREF_FAVORITES, new HashSet<>());
        Set<String> newFavorites = new HashSet<>(favorites);
        newFavorites.remove(cityName);
        preferences.edit().putStringSet(MainActivity.PREF_FAVORITES, newFavorites).apply();
    }
}