package mx.edu.itson.proyectonova.services.impl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.edu.itson.proyectonova.exceptions.ResourceNotFoundException;
import mx.edu.itson.proyectonova.models.dtos.DatosFisicosResponse;
import mx.edu.itson.proyectonova.models.entities.KeplerDataEntity;
import mx.edu.itson.proyectonova.repositories.KeplerRepository;
import mx.edu.itson.proyectonova.services.interfaces.ExploracionService;
import mx.edu.itson.proyectonova.mappers.KeplerMapper;
import mx.edu.itson.proyectonova.utils.SoftmaxMathUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import mx.edu.itson.proyectonova.utils.SoftmaxMathUtil;
import mx.edu.itson.proyectonova.models.dtos.DatosFisicosResponse;

import java.io.InputStream;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExploracionServiceImpl implements ExploracionService {

    private final KeplerRepository repository;
    private final KeplerMapper mapper;
    private OrtEnvironment env;
    private OrtSession session;

    @PostConstruct
    public void inicializarMotorIA() {
        try {
            log.info("Iniciando motor de Machine Learning (ONNX Runtime)...");
            this.env = OrtEnvironment.getEnvironment();
            ClassPathResource resource = new ClassPathResource("ai/nova_modelo.onnx");
            try (InputStream is = resource.getInputStream()) {
                byte[] modelBytes = is.readAllBytes();
                this.session = env.createSession(modelBytes, new OrtSession.SessionOptions());
                log.info("Modelo ONNX cargado exitosamente.");
            }
        } catch (Exception e) {
            log.error("Fallo critico al inicializar la Red Neuronal ONNX: ", e);
        }
    }

    @PreDestroy
    public void apagarMotorIA() {
        try {
            if (this.session != null) this.session.close();
            if (this.env != null) this.env.close();
            log.info("Motor ONNX Runtime apagado limpiamente.");
        } catch (OrtException e) {
            log.error("Error al liberar recursos graficos ONNX: ", e);
        }
    }

    @Override
    public DatosFisicosResponse analizarAstro(String kepid) {
        KeplerDataEntity entity = repository.findByKepid(kepid)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro telemetria para el astro: " + kepid));

        float[] vectorCaracteristicas = extraerVectorParaIA(entity);
        float[] logitsRaw = ejecutarInferencia(vectorCaracteristicas);
        double[] probabilidades = SoftmaxMathUtil.calcularDistribucion(logitsRaw);

        double confianzaExoplaneta = probabilidades[1];
        boolean esConfirmado = confianzaExoplaneta > 0.85;

// 5. Ensamblar DTO final para el frontend WebGL
        DatosFisicosResponse response = mapper.toDatosFisicosResponse(entity);
        response.setProbabilidadExoplaneta(confianzaExoplaneta);
        response.setConfirmadoPorIA(esConfirmado);

        return response;
    }

    private float[] ejecutarInferencia(float[] features) {
        try {
            float[][] inputMatrix = new float[][]{ features };
            try (OnnxTensor tensorEntrada = OnnxTensor.createTensor(this.env, inputMatrix)) {
                String nombreInput = this.session.getInputNames().iterator().next();
                try (OrtSession.Result resultado = this.session.run(Collections.singletonMap(nombreInput, tensorEntrada))) {
                    float[][] outputMatrix = (float[][]) resultado.get(0).getValue();
                    return outputMatrix[0];
                }
            }
        } catch (OrtException e) {
            log.error("Colapso durante el calculo del tensor ONNX: ", e);
            throw new RuntimeException("Error en la unidad de procesamiento de IA.");
        }
    }

    private float[] extraerVectorParaIA(KeplerDataEntity e) {
        // El orden de estas 25 variables debe coincidir EXACTAMENTE
        // con la matriz de entrada con la que entrenaste el modelo en Python.
        return new float[] {
                e.getKoiPeriod() != null ? e.getKoiPeriod().floatValue() : 0f,
                e.getKoiPeriodErr1() != null ? e.getKoiPeriodErr1().floatValue() : 0f,
                e.getKoiTime0bk() != null ? e.getKoiTime0bk().floatValue() : 0f,
                e.getKoiTime0bkErr1() != null ? e.getKoiTime0bkErr1().floatValue() : 0f,
                e.getKoiImpact() != null ? e.getKoiImpact().floatValue() : 0f,
                e.getKoiImpactErr1() != null ? e.getKoiImpactErr1().floatValue() : 0f,
                e.getKoiDuration() != null ? e.getKoiDuration().floatValue() : 0f,
                e.getKoiDurationErr1() != null ? e.getKoiDurationErr1().floatValue() : 0f,
                e.getKoiDepth() != null ? e.getKoiDepth().floatValue() : 0f,
                e.getKoiDepthErr1() != null ? e.getKoiDepthErr1().floatValue() : 0f,
                e.getKoiPrad() != null ? e.getKoiPrad().floatValue() : 0f,
                e.getKoiPradErr1() != null ? e.getKoiPradErr1().floatValue() : 0f,
                e.getKoiTeq() != null ? e.getKoiTeq().floatValue() : 0f,
                e.getKoiInsol() != null ? e.getKoiInsol().floatValue() : 0f,
                e.getKoiInsolErr1() != null ? e.getKoiInsolErr1().floatValue() : 0f,
                e.getKoiModelSnr() != null ? e.getKoiModelSnr().floatValue() : 0f,
                e.getKoiSteff() != null ? e.getKoiSteff().floatValue() : 0f,
                e.getKoiSteffErr1() != null ? e.getKoiSteffErr1().floatValue() : 0f,
                e.getKoiSlogg() != null ? e.getKoiSlogg().floatValue() : 0f,
                e.getKoiSloggErr1() != null ? e.getKoiSloggErr1().floatValue() : 0f,
                e.getKoiSrad() != null ? e.getKoiSrad().floatValue() : 0f,
                e.getKoiSradErr1() != null ? e.getKoiSradErr1().floatValue() : 0f,
                e.getRa() != null ? e.getRa().floatValue() : 0f,
                e.getDec() != null ? e.getDec().floatValue() : 0f,
                e.getKoiKepmag() != null ? e.getKoiKepmag().floatValue() : 0f
        };
    }
}