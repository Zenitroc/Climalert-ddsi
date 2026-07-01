package ar.edu.utn.dds.climalert.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClimaService {

    public void obtenerClimaActual() {
        log.info("Obteniendo clima actual...");
    }

    public void analizarAlertas() {
        log.info("Analizando alertas climáticas...");
    }
}