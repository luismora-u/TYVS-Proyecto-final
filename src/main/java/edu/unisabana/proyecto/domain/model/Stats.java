package edu.unisabana.proyecto.domain.model;

/**
 * Estadisticas agregadas del padron de votantes registrados.
 *
 * @param total      cantidad de votantes registrados
 * @param averageAge edad promedio (0.0 si no hay registros)
 * @param minAge     edad minima (0 si no hay registros)
 * @param maxAge     edad maxima (0 si no hay registros)
 */
public record Stats(long total, double averageAge, int minAge, int maxAge) {
}
