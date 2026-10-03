/**
 * src/main/resources/static/js/app.js
 * Punto de entrada principal y orquestador de la interfaz de usuario.
 * Une el ApiClient (puerto 9090), el Motor3DManager y el DOM del index.html.
 *
 * Se utiliza el estandar de Modulos ES nativos.
 */

import { Motor3DManager } from './Motor3DManager.js';
import { NovaApi } from './ApiClient.js';

// Zona de Diagnostico Tecnico - Medellin 2024
console.log('Orquestador App Nova inicializado. Suministrando energia al motor...');

// 1. Punto de entrada al cargar el DOM completo
window.addEventListener('DOMContentLoaded', () => {

    // Referencia obligatoria al Canvas HTML para Three.js
    const canvas = document.getElementById('novaCanvas');

    // Instanciacion del Motor 3D (Arranque de Shaders y fisicas)
    // Se delega todo el manejo grafico a esta clase.
    const motorManager = new Motor3DManager(canvas);

    // Referencias a elementos UI de index.html
    const inputKep = document.getElementById('kepidInput');
    const btnSearch = document.getElementById('searchBtn');
    const msgLoading = document.getElementById('loadingMessage');
    const msgError = document.getElementById('errorMessage');
    const panelData = document.getElementById('dataDisplay');

    // 2. Logica de Negocio: Ejecutar Exploracion de Exoplaneta
    /**
     * Coordina la llamada a la API local (9090) y la actualizacion visual.
     * @param {string} kepid Identificador de la NASA.
     */
    const realizarExploracion = async (kepid) => {
        // Validación básica de entrada
        if (!kepid || kepid.trim() === "") {
            console.warn('Exploracion cancelada: Kepid vacio.');
            return;
        }

        console.log('Iniciando exploracion para: ' + kepid);

        // A. Reset UI: Ocultar datos previos/errores y mostrar estado de carga
        panelData.classList.add('hide');
        msgError.classList.add('hide');
        msgLoading.classList.remove('hide');

        // B. CONSULTA AL BACKEND SPRING BOOT (port 9090)
        // NovaApi (ApiClient.js) usa fetch nativo y gestiona los estatus 404/500/Red
        const respuesta = await NovaApi.getTelemetriaExoplaneta(kepid);

        // Ocultamos mensaje de carga independientemente del resultado
        msgLoading.classList.add('hide');

        // C. Manejo centralizado de la respuesta
        if (respuesta.error) {
            // ERROR CONTROLADO: El ApiClient devolvio un objeto estructurado de error
            console.error('Exploracion fallida: ' + respuesta.message);
            msgError.textContent = respuesta.message;
            msgError.classList.remove('hide');

            // Opcional: El motor 3D podria pausarse o mostrar un estado de "error"
            // motorManager.pausarMotor();
        } else {
            // EXITO: Recibimos DTO DatosFisicosResponse de Java
            console.log('Telemetria AAA recibida. Sincronizando motor grafico...', respuesta);

            // 1. INYECCION DE DATOS EN EL MOTOR 3D
            // Actualiza Shaders de plasma, biomas de lava/hielo y fisicas de Kepler
            motorManager.inyectarTelemetriaNASA(respuesta);
            motorManager.reanudarMotor(); // Asegurar que corre si estaba pausado

            // 2. Actualizacion del Panel Metrico de la UI (Numbers)
            // Se inyectan los valores directamente en los spans del HTML
            document.getElementById('valKepid').textContent = respuesta.kepid || '--';
            document.getElementById('valTemp').textContent = respuesta.temperaturaEstelar || '--';
            document.getElementById('valGravedad').textContent = respuesta.gravedadEstelar || '--';
            document.getElementById('valRadio').textContent = respuesta.radioPlanetario || '--';
            document.getElementById('valDia').textContent = respuesta.duracionOrbital || '--';
            document.getElementById('valTempEq').textContent = respuesta.temperaturaEquilibrio || '--';

            // Muestra el panel con las metricas
            panelData.classList.remove('hide');
        }
    };

    // 3. Vinculacion de Eventos de la Interfaz
    // Click en boton "Explorar"
    btnSearch.addEventListener('click', () => {
        realizarExploracion(inputKep.value);
    });

    // Tecla "Enter" en el input
    inputKep.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            realizarExploracion(inputKep.value);
        }
    });

    // Clicks en la lista rápida de demostración AAA
    // Se usa delegacion de eventos para los botones kep-link
    document.querySelectorAll('.kep-link').forEach(link => {
        link.addEventListener('click', (e) => {
            const kepId = e.target.getAttribute('data-kep');
            inputKep.value = kepId; // Sincroniza el input
            realizarExploracion(kepId);
        });
    });

    // 4. SEGURIDAD DE VRAM (Memory Management)
    // Cuando se cierra o recarga la pestaña, forzamos el purgado profundo del 3D
    // para liberar la RTX 4060 de la MSI Katana.
    window.addEventListener('unload', () => {
        if (motorManager) motorManager.destruirYApagar();
    });

    console.log('Interfaz de Usuario Nova y Motor AAA acoplados y listos.');
});