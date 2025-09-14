package com.example.apalaka;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        LinearLayout layout = findViewById(R.id.splash_layout);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 16 || hour < 6) {
            layout.setBackgroundResource(R.drawable.weather_bg_night);
        } else {
            layout.setBackgroundResource(R.drawable.weather_bg);
        }

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);
    }
}
