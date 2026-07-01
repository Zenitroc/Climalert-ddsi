package ar.edu.utn.dds.climalert.service;

import ar.edu.utn.dds.climalert.domain.AlertaClimatica;
import ar.edu.utn.dds.climalert.domain.MedicionClima;
import ar.edu.utn.dds.climalert.integration.WeatherApiClient;
import ar.edu.utn.dds.climalert.integration.dto.WeatherApiResponse;
import ar.edu.utn.dds.climalert.repository.AlertaClimaticaRepository;
import ar.edu.utn.dds.climalert.repository.MedicionClimaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClimaService {

    private final MedicionClimaRepository medicionClimaRepository;
    private final AlertaClimaticaRepository alertaClimaticaRepository;
    private final NotificadorEmailService notificadorEmailService;
    private final WeatherApiClient weatherApiClient;

    public void obtenerClimaActual() {
        WeatherApiResponse response = weatherApiClient.obtenerClimaActual();

        MedicionClima medicion = new MedicionClima();

        medicion.setUbicacion(obtenerUbicacion(response));
        medicion.setTemperaturaC(response.current().tempC());
        medicion.setHumedad(response.current().humidity());
        medicion.setCondicion(response.current().condition().text());
        medicion.setVientoKph(response.current().windKph());
        medicion.setPresionMb(response.current().pressureMb());
        medicion.setFechaMedicionProveedor(response.current().lastUpdated());
        medicion.setFechaRegistro(LocalDateTime.now());

        medicionClimaRepository.save(medicion);

        log.info(
                "Medición guardada desde WeatherAPI: ubicación={}, temperatura={}°C, humedad={}%, condición={}",
                medicion.getUbicacion(),
                medicion.getTemperaturaC(),
                medicion.getHumedad(),
                medicion.getCondicion()
        );
    }

    public void analizarAlertas() {
        medicionClimaRepository.findFirstByOrderByFechaRegistroDesc()
                .ifPresentOrElse(
                        this::analizarMedicion,
                        () -> log.info("No hay mediciones para analizar todavía")
                );
    }

    private void analizarMedicion(MedicionClima medicion) {
        log.info(
                "Analizando medición id={}: temperatura={}°C, humedad={}%",
                medicion.getId(),
                medicion.getTemperaturaC(),
                medicion.getHumedad()
        );

        if (!medicion.alerta()) {
            log.info("La medición no representa una alerta climática");
            return;
        }

        if (alertaClimaticaRepository.existsByMedicionClimaId(medicion.getId())) {
            log.info("La medición crítica ya tenía una alerta generada");
            return;
        }

        generarAlerta(medicion);
    }

    private void generarAlerta(MedicionClima medicion) {
        String mensaje = """
                Alerta climática detectada.

                Ubicación: %s
                Temperatura: %.2f °C
                Humedad: %d %%
                Condición: %s
                Viento: %.2f kph
                Presión: %.2f mb
                Fecha proveedor: %s
                Fecha de registro local: %s
                """.formatted(
                medicion.getUbicacion(),
                medicion.getTemperaturaC(),
                medicion.getHumedad(),
                medicion.getCondicion(),
                medicion.getVientoKph(),
                medicion.getPresionMb(),
                medicion.getFechaMedicionProveedor(),
                medicion.getFechaRegistro()
        );

        AlertaClimatica alerta = new AlertaClimatica();
        alerta.setMedicionClima(medicion);
        alerta.setMensaje(mensaje);
        alerta.setFechaGeneracion(LocalDateTime.now());

        alertaClimaticaRepository.save(alerta);

        notificadorEmailService.enviarAlerta(mensaje);

        log.warn("ALERTA CLIMÁTICA GENERADA para la medición id={}", medicion.getId());
    }

    private String obtenerUbicacion(WeatherApiResponse response) {
        return "%s, %s, %s".formatted(
                response.location().name(),
                response.location().region(),
                response.location().country()
        );
    }
}