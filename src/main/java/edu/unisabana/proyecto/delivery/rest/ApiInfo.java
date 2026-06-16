package edu.unisabana.proyecto.delivery.rest;

import java.util.Map;

/**
 * Indice de la API expuesto en la raiz {@code GET /}: nombre del servicio, version
 * y mapa de endpoints disponibles con su descripcion.
 */
public record ApiInfo(String servicio, String version, Map<String, String> endpoints) {
}
