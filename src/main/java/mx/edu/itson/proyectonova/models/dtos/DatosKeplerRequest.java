package mx.edu.itson.proyectonova.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada. Recibe telemetría astrofísica manual desde el cliente
 * para inyectarla directamente al modelo ONNX sin pasar por la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosKeplerRequest {

    // 1-2: Período Orbital
    private Float koiPeriod;
    private Float koiPeriodErr1;

    // 3-4: Tiempo de Tránsito
    private Float koiTime0bk;
    private Float koiTime0bkErr1;

    // 5-6: Parámetro de Impacto
    private Float koiImpact;
    private Float koiImpactErr1;

    // 7-8: Duración del Tránsito
    private Float koiDuration;
    private Float koiDurationErr1;

    // 9-10: Profundidad del Tránsito
    private Float koiDepth;
    private Float koiDepthErr1;

    // 11-12: Radio Planetario
    private Float koiPrad;
    private Float koiPradErr1;

    // 13: Temperatura de Equilibrio Planetario
    private Float koiTeq;

    // 14-15: Insolación Planetaria
    private Float koiInsol;
    private Float koiInsolErr1;

    // 16: Relación Señal-Ruido del Tránsito
    private Float koiModelSnr;

    // 17-18: Temperatura Efectiva Estelar
    private Float koiSteff;
    private Float koiSteffErr1;

    // 19-20: Gravedad Superficial Estelar
    private Float koiSlogg;
    private Float koiSloggErr1;

    // 21-22: Radio Estelar
    private Float koiSrad;
    private Float koiSradErr1;

    // 23-25: Coordenadas Celestiales y Magnitud
    private Float ra;
    private Float dec;
    private Float koiKepmag;
}