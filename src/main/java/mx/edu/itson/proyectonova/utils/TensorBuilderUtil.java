package mx.edu.itson.proyectonova.utils;

import lombok.extern.slf4j.Slf4j;
import mx.edu.itson.proyectonova.models.entities.KeplerDataEntity;
import mx.edu.itson.proyectonova.exceptions.TensorBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class TensorBuilderUtil {
    /**
     * Extrae las 25 variables fisicas de la entidad y construye un arreglo bidimensional
     * se aplica imputacion basica a los valores nulos (o.0f) para evitar NullPointerException
     */
    public static float[][] construirTensor(KeplerDataEntity entidad) {
        try{
            float[] caracteristicas = new float[25];
            // 1-2: Período Orbital
            caracteristicas[0] = sanear(entidad.getKoiPeriod());
            caracteristicas[1] = sanear(entidad.getKoiPeriodErr1());

            // 3-4: Tiempo de Tránsito
            caracteristicas[2] = sanear(entidad.getKoiTime0bk());
            caracteristicas[3] = sanear(entidad.getKoiTime0bkErr1());

            // 5-6: Parámetro de Impacto
            caracteristicas[4] = sanear(entidad.getKoiImpact());
            caracteristicas[5] = sanear(entidad.getKoiImpactErr1());

            // 7-8: Duración del Tránsito
            caracteristicas[6] = sanear(entidad.getKoiDuration());
            caracteristicas[7] = sanear(entidad.getKoiDurationErr1());

            // 9-10: Profundidad del Tránsito
            caracteristicas[8] = sanear(entidad.getKoiDepth());
            caracteristicas[9] = sanear(entidad.getKoiDepthErr1());

            // 11-12: Radio Planetario
            caracteristicas[10] = sanear(entidad.getKoiPrad());
            caracteristicas[11] = sanear(entidad.getKoiPradErr1());

            // 13: Temperatura de Equilibrio
            caracteristicas[12] = sanear(entidad.getKoiTeq());

            // 14-15: Insolación Planetaria
            caracteristicas[13] = sanear(entidad.getKoiInsol());
            caracteristicas[14] = sanear(entidad.getKoiInsolErr1());

            // 16: Relación Señal-Ruido
            caracteristicas[15] = sanear(entidad.getKoiModelSnr());

            // 17-18: Temperatura Efectiva Estelar
            caracteristicas[16] = sanear(entidad.getKoiSteff());
            caracteristicas[17] = sanear(entidad.getKoiSteffErr1());

            // 19-20: Gravedad Superficial
            caracteristicas[18] = sanear(entidad.getKoiSlogg());
            caracteristicas[19] = sanear(entidad.getKoiSloggErr1());

            // 21-22: Radio Estelar
            caracteristicas[20] = sanear(entidad.getKoiSrad());
            caracteristicas[21] = sanear(entidad.getKoiSradErr1());

            // 23-25: Coordenadas y Magnitud
            caracteristicas[22] = sanear(entidad.getRa());
            caracteristicas[23] = sanear(entidad.getDec());
            caracteristicas[24] = sanear(entidad.getKoiKepmag());

            // Retornamos un tensor de forma [1, 25] (1 registro, 25 características)
            return new float[][]{caracteristicas};
        }catch(Exception e){
            log.error("Fallo crítico al construir el tensor de características para el KEPID: " + entidad.getKepid(), e);
            throw new TensorBuildException("Fallo crítico al construir el tensor de características para el KEPID: " + entidad.getKepid(), e);
        }
    }
    /**
     * Imputación defensiva: Si el valor es nulo, retorna 0.0f.
     */
    private static float sanear(Float valor) {
        return (valor != null) ? valor : 0.0f;
    }
}
