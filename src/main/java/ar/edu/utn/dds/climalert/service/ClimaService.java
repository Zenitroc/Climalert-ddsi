package ar.edu.utn.dds.climalert.service;

import ar.edu.utn.dds.climalert.domain.AlertaClimatica;
import ar.edu.utn.dds.climalert.domain.MedicionClima;
import ar.edu.utn.dds.climalert.repository.AlertaClimaticaRepository;
import ar.edu.utn.dds.climalert.repository.MedicionClimaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
public class ClimaService {

    private final MedicionClimaRepository medicionClimaRepository;
    private final AlertaClimaticaRepository alertaClimaticaRepository;
    private final NotificadorEmailService notificadorEmailService;

    public ClimaService(MedicionClimaRepository medicionClimaRepository, AlertaClimaticaRepository alertaClimaticaRepository, NotificadorEmailService notificadorEmailService) {
        this.medicionClimaRepository = medicionClimaRepository;
        this.alertaClimaticaRepository = alertaClimaticaRepository;
        this.notificadorEmailService = notificadorEmailService;
    }

    public void obtenerClimaActual() {
        MedicionClima medicion = new MedicionClima();

        medicion.setUbicacion("CABA");
        medicion.setTemperaturaC(temperaturaMock()); //PARA TESTEAR Q FUNQUE
        medicion.setHumedad(humedadMock()); //PARA TESTEAR Q FUNQUE
        medicion.setCondicion("Simulado");
        medicion.setFechaRegistro(LocalDateTime.now());

        medicionClimaRepository.save(medicion);

        log.info(
                "Medición guardada: ubicación={}, temperatura={}°C, humedad={}%",
                medicion.getUbicacion(),
                medicion.getTemperaturaC(),
                medicion.getHumedad()
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
                Alerta climática detectada!

                Ubicación: %s
                Temperatura: %.2f °C
                Humedad: %d %%
                Condición: %s
                Fecha de registro: %s
                """.formatted(
                medicion.getUbicacion(),
                medicion.getTemperaturaC(),
                medicion.getHumedad(),
                medicion.getCondicion(),
                medicion.getFechaRegistro()
        );

        AlertaClimatica alerta = new AlertaClimatica();
        alerta.setMedicionClima(medicion);
        alerta.setMensaje(mensaje);
        alerta.setFechaGeneracion(LocalDateTime.now());

        alertaClimaticaRepository.save(alerta);

        notificadorEmailService.enviarAlerta(mensaje); //Envio mail

        log.warn("ALERTA CLIMÁTICA GENERADA para la medición id={}", medicion.getId());
    }

    private Double temperaturaMock() {
        return 25 + new Random().nextDouble(15);
    }

    private Integer humedadMock() {
        return 40 + new Random().nextInt(40);
    }
}