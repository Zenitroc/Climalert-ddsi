package ar.edu.utn.dds.climalert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificadorEmailService {

    private final RestClient mailtrapRestClient;

    @Value("${mailtrap.token}")
    private String token;

    @Value("${mailtrap.from-email}")
    private String fromEmail;

    @Value("${mailtrap.from-name}")
    private String fromName;

    @Value("${mailtrap.category}")
    private String category;

    public void enviarAlerta(String mensaje) {
        if (!StringUtils.hasText(token)) {
            log.warn("No se envió el mail porque MAILTRAP_TOKEN no está configurado en variable de entorno");
            return;
        }

        MailtrapEmailRequest request = new MailtrapEmailRequest(
                new MailtrapAddress(fromEmail, fromName),
                List.of(
                        new MailtrapAddress("admin@clima.com", "Administración"),
                        new MailtrapAddress("emergencias@clima.com", "Emergencias"),
                        new MailtrapAddress("meteorologia@clima.com", "Meteorología")
                ),
                "Alerta climática - Climalert",
                mensaje,
                category
        );

        try {
            mailtrapRestClient.post()
                    .uri("/api/send")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Correo de alerta enviado correctamente mediante Mailtrap API");
        } catch (RestClientResponseException e) {
            log.error(
                    "Error enviando mail por Mailtrap. Status: {}. Body: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            log.error("Error inesperado enviando mail por Mailtrap", e);
        }
    }

    private record MailtrapEmailRequest(
            MailtrapAddress from,
            List<MailtrapAddress> to,
            String subject,
            String text,
            String category
    ) {
    }

    private record MailtrapAddress(
            String email,
            String name
    ) {
    }
}