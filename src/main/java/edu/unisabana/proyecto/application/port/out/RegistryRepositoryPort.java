package edu.unisabana.proyecto.application.port.out;

import edu.unisabana.proyecto.infrastructure.persistence.RegistryRecord;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de registros.
 *
 * <p>Define las operaciones que necesita el caso de uso {@code Registry} sin
 * acoplarse a la tecnologia de persistencia (JDBC, JPA, etc.). Esto permite
 * sustituir la implementacion real por un doble de prueba (Mockito) o por una
 * base de datos en memoria (H2) en las pruebas de integracion.</p>
 */
public interface RegistryRepositoryPort {

    /** Crea la tabla/estructura inicial (util en pruebas con H2). */
    void initSchema() throws Exception;

    /** Verifica si un registro existe por su documento. */
    boolean existsById(int id) throws Exception;

    /** Persiste un nuevo registro. */
    void save(int id, String name, int age, boolean isAlive) throws Exception;

    /** Busca un registro por su documento. */
    Optional<RegistryRecord> findById(int id) throws Exception;

    /** Borra todos los registros (util para aislar pruebas). */
    void deleteAll() throws Exception;
}
