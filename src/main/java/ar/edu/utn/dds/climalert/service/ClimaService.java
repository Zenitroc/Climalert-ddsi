package ar.edu.utn.dds.climalert.service;

import ar.edu.utn.dds.climalert.domain.MedicionClima;
import ar.edu.utn.dds.climalert.repository.MedicionClimaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
public class ClimaService {

    private final MedicionClimaRepository medicionClimaRepository;

    public ClimaService(MedicionClimaRepository medicionClimaRepository) {
        this.medicionClimaRepository = medicionClimaRepository;
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
                "Analizando medición: temperatura={}°C, humedad={}%",
                medicion.getTemperaturaC(),
                medicion.getHumedad()
        );

        if (medicion.alerta()) {
            log.warn("ALERTA CLIMÁTICA: la temperatura superó los 35°C y humedad es mayor a 60%");
        } else {
            log.info("La medición no representa una alerta climática");
        }
    }

    private Double temperaturaMock() {
        return 25 + new Random().nextDouble(15);
    }

    private Integer humedadMock() {
        return 40 + new Random().nextInt(40);
    }
}