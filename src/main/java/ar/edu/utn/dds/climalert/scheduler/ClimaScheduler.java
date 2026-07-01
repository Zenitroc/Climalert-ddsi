package ar.edu.utn.dds.climalert.scheduler;

import ar.edu.utn.dds.climalert.service.ClimaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClimaScheduler {

    private final ClimaService climaService;

    @Scheduled(cron = "${climalert.scheduler.obtener-clima}")
    public void obtenerClimaActual() {
        climaService.obtenerClimaActual();
    }

    @Scheduled(cron = "${climalert.scheduler.analizar-alertas}")
    public void analizarAlertas() {
        climaService.analizarAlertas();
    }
}