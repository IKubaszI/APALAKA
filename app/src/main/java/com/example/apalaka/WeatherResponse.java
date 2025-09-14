package com.example.apalaka;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {
    @SerializedName("name")
    public String name;

    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public List<Weather> weather;

    public class Main {
        @SerializedName("temp")
        public float temp;

        @SerializedName("humidity")
        public float humidity;
    }

    public class Weather {
        @SerializedName("main")
        public String main;

        @SerializedName("description")
        public String description;

        @SerializedName("icon")
        public String icon;
    }
}
