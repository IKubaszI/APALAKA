package com.example.apalaka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "e7b60b26d60a3c6bdb8d707cdc3f8b9b";
    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static final String ICON_URL = "https://openweathermap.org/img/wn/";

    // Elementy UI
    private EditText editTextCity;
    private Button buttonGetWeather;
    private TextView textViewCityName;
    private TextView textViewTemperature;
    private TextView textViewDescription;
    private TextView textViewHumidity;
    private ImageView imageViewWeatherIcon;

    private WeatherService weatherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setupRetrofit();

        buttonGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = editTextCity.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    getCurrentWeatherData(cityName);
                } else {
                    Toast.makeText(MainActivity.this, "Wprowadź nazwę miasta", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getCurrentWeatherData("Warsaw");
    }

    private void initViews() {
        editTextCity = findViewById(R.id.editTextCity);
        buttonGetWeather = findViewById(R.id.buttonGetWeather);
        textViewCityName = findViewById(R.id.textViewCityName);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewHumidity = findViewById(R.id.textViewHumidity);
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = retrofit.create(WeatherService.class);
    }

    private void getCurrentWeatherData(String cityName) {
        Call<WeatherResponse> call = weatherService.getCurrentWeather(cityName, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayWeatherData(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "Nie znaleziono miasta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Błąd połączenia: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWeatherData(WeatherResponse weatherResponse) {

        textViewCityName.setText(weatherResponse.name);

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

            // Załaduj ikonę pogody
            if (weather.icon != null) {
                String iconUrl = ICON_URL + weather.icon + "@2x.png";
                Glide.with(this)
                        .load(iconUrl)
                        .into(imageViewWeatherIcon);
            }
        }
    }
}