package ar.edu.utn.dds.climalert.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class MedicionClima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ubicacion;
    private Double temperaturaC;
    private Integer humedad;
    private String condicion;
    private Double vientoKph;
    private Double presionMb;
    private String fechaMedicionProveedor;
    private LocalDateTime fechaRegistro;

    public boolean alerta() {
        return temperaturaC != null
                && humedad != null
                && temperaturaC > 35
                && humedad > 60;
    }
}