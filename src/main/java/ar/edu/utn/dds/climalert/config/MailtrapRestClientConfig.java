package ar.edu.utn.dds.climalert.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MailtrapRestClientConfig {

    @Bean
    public RestClient mailtrapRestClient(
            @Value("${mailtrap.base-url}") String baseUrl
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}