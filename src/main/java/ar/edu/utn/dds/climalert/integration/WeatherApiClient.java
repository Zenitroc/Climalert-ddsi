package ar.edu.utn.dds.climalert.integration;

import ar.edu.utn.dds.climalert.integration.dto.WeatherApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WeatherApiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String location;

    public WeatherApiClient(
            @Value("${climalert.weather.base-url}") String baseUrl,
            @Value("${climalert.weather.api-key}") String apiKey,
            @Value("${climalert.weather.location}") String location
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
        this.location = location;
    }

    public WeatherApiResponse obtenerClimaActual() {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("WEATHER_API_KEY no está configurada");
        }

        log.info("Consultando WeatherAPI para ubicación {}", location);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", location)
                        .queryParam("lang", "es")
                        .build())
                .retrieve()
                .body(WeatherApiResponse.class);
    }
}