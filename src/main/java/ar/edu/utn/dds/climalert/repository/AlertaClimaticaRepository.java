package ar.edu.utn.dds.climalert.repository;

import ar.edu.utn.dds.climalert.domain.AlertaClimatica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaClimaticaRepository extends JpaRepository<AlertaClimatica, Long> {

    boolean existsByMedicionClimaId(Long medicionClimaId);
}