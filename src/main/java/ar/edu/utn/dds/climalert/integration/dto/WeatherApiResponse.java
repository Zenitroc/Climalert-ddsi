package ar.edu.utn.dds.climalert.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherApiResponse(
        Location location,
        Current current
) {
    public record Location(
            String name,
            String region,
            String country,
            String localtime
    ) {
    }

    public record Current(
            @JsonProperty("last_updated")
            String lastUpdated,

            @JsonProperty("temp_c")
            Double tempC,

            Integer humidity,

            Condition condition,

            @JsonProperty("wind_kph")
            Double windKph,

            @JsonProperty("pressure_mb")
            Double pressureMb
    ) {
    }

    public record Condition(
            String text
    ) {
    }
}