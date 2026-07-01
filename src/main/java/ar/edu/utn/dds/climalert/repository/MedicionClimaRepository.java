package ar.edu.utn.dds.climalert.repository;

import ar.edu.utn.dds.climalert.domain.MedicionClima;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicionClimaRepository extends JpaRepository<MedicionClima, Long> {

    Optional<MedicionClima> findFirstByOrderByFechaRegistroDesc();
}