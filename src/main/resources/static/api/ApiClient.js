const API_BASE_URL = 'http://localhost:9090';

console.log('ApiClient Nova configurado para: ' + API_BASE_URL);

// Cabeceras estandar para garantizar compatibilidad con Spring Boot @RestController
const standardHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
};

// 2. Helper centralizado para Fetch con manejo de errores robusto
// Reemplaza la funcionalidad de Axios interceptors para entorno Vanila.
const fetchApi = async (endpoint, options = {}) => {
    const url = `${API_BASE_URL}${endpoint}`;

    // Mezclar opciones y cabeceras estandar
    const config = {
        ...options,
        headers: {
            ...standardHeaders,
            ...options.headers
        }
    };

    try {
        const response = await fetch(url, config);

        // Intentamos parsear JSON, asumiendo que el backend siempre responde JSON
        // o un DTO de error estructurado.
        const data = await response.json();

        // A. Verificar si la respuesta HTTP es exitosa (200-299)
        if (!response.ok) {
            // Zona de Diagnostico Tecnico - Medellin 2024
            let mensajeErrorAmigable = 'Ocurrio un error inesperado al conectar con el servidor Nova.';
            const status = response.status;

            console.error('Error del Backend Java Nova. Estatus: ' + status, data);

            // Personalizar mensaje segun Estatus HTTP de Spring Boot
            if (status === 404) {
                // Se extrae el KEPID del endpoint solicitado para el mensaje
                const parts = endpoint.split('/');
                const kepidSolicitado = parts[parts.length - 1];
                mensajeErrorAmigable = 'El astro solicitado (KEPID: ' + kepidSolicitado + ') no existe en el catalogo local.';
            } else if (status === 500) {
                mensajeErrorAmigable = 'El motor de Exploracion del backend colapso inesperadamente. Verifique logs de Java.';
            } else if (data && data.message) {
                // Usar mensaje especifico enviado por GlobalExceptionHandler.java si existe
                mensajeErrorAmigable = data.message;
            }

            // Devolver objeto de error estructurado para que main.js actualice la UI suavemente
            return {
                error: true,
                status: status,
                message: mensajeErrorAmigable
            };
        }

        // B. EXITO: Si todo esta bien, devolvemos la data purgada (DTO DatosFisicosResponse)
        return data;

    } catch (error) {
        // C. Error de Red o Servidor Apagado (Catch fatal de fetch)
        // La peticion nunca llego o el servidor no respondio (Network Error / Timeout).
        console.error('Error de Red o Servidor Java Apagado. Detalle:', error.message);

        // El puerto configurado debe ser 9090 en Gradle.kts
        return {
            error: true,
            status: 'NETWORK_ERROR',
            message: 'Imposible conectar con el servidor local. Asegurese de que Spring Boot este encendido en el puerto 9090.'
        };
    }
};

/**
 * 3. API de Dominio: Metodos Especificos para el Proyecto Nova
 */
export const NovaApi = {

    /**
     * Solicita la telemetria fisica de un exoplaneta especifico al controlador de Spring Boot.
     * Reacciona a los Shaders y fisicas de Three.js en el MSI Katana.
     *
     * @param {string} kepid Identificador oficial (Kepler ID).
     * @returns {Promise<Object>} Datos purgados (DatosFisicosResponse) o un objeto de error estructurado para la UI.
     */
    async getTelemetriaExoplaneta(kepid) {
        if (!kepid) throw new Error('ApiClient: Kepid es obligatorio.');

        console.log('ApiClient: Solicitando telemetria para KEPID: ' + kepid);

        // Coincide exactamente con @RequestMapping("/api/exploracion/{kepid}") en Java
        // y @CrossOrigin habilitado en el controlador.
        return fetchApi(`/api/exploracion/${kepid}`, {
            method: 'GET'
            // Podrias agregar credentials: 'include' si implementas seguridad mas adelante
        });
    }
};