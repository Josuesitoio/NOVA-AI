package mx.edu.itson.proyectonova.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.edu.itson.proyectonova.models.dtos.DatosFisicosResponse;
import mx.edu.itson.proyectonova.services.interfaces.ExploracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST principal para la interfaz de telemetria espacial.
 * Conecta el frontend 3D con el motor de inferencia ONNX Runtime.
 */
@Slf4j
@RestController
@RequestMapping("/api/exploracion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Crucial para evitar bloqueos CORS si ejecutas el HTML directo en el navegador
public class ExploracionController {

    private final ExploracionService exploracionService;

    /**
     * Recupera la telemetria fisica y ejecuta la prediccion de IA para un astro.
     *
     * @param kepid Identificador oficial del catalogo Kepler (Ej. K00001.01).
     * @return DTO empaquetado con estatus HTTP 200 OK.
     */
    @GetMapping("/{kepid}")
    public ResponseEntity<DatosFisicosResponse> obtenerTelemetria(@PathVariable String kepid) {
        log.info("Iniciando secuencia de exploracion para el astro: {}", kepid);

        // La logica pesada (SQLite + ONNX + Softmax) ocurre dentro de la capa de servicio
        DatosFisicosResponse respuesta = exploracionService.analizarAstro(kepid);

        log.info("Telemetria y prediccion listas. Transmitiendo al cliente WebGL.");
        return ResponseEntity.ok(respuesta);
    }
}