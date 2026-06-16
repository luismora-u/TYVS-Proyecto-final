package edu.unisabana.proyecto.application.usecase;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.RegisterResult;
import edu.unisabana.proyecto.domain.model.Stats;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRecord;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso de la registraduria: registra votantes aplicando las reglas de
 * negocio y delegando la persistencia en el puerto {@link RegistryRepositoryPort}.
 *
 * <p>Reglas:</p>
 * <ol>
 *   <li>Solo se registran votantes validos.</li>
 *   <li>Solo se permite una inscripcion por documento.</li>
 * </ol>
 */
public class Registry {

    /** Edad minima para poder votar (inclusive). */
    public static final int MIN_AGE = 18;

    private final RegistryRepositoryPort repo;

    public Registry(RegistryRepositoryPort repo) {
        this.repo = repo;
    }

    /**
     * Intenta registrar a una persona como votante.
     *
     * @param p persona a registrar
     * @return el resultado del intento de registro
     */
    public RegisterResult registerVoter(Person p) {
        if (p == null) {
            return RegisterResult.INVALID;
        }
        if (p.getId() <= 0) {
            return RegisterResult.INVALID;
        }
        if (!p.isAlive()) {
            return RegisterResult.DEAD;
        }
        if (p.getAge() < MIN_AGE) {
            return RegisterResult.UNDERAGE;
        }

        try {
            if (repo.existsById(p.getId())) {
                return RegisterResult.DUPLICATED;
            }
            repo.save(p.getId(), p.getName(), p.getAge(), p.isAlive());
            return RegisterResult.VALID;
        } catch (Exception e) {
            throw new IllegalStateException("Error de persistencia: " + e.getMessage(), e);
        }
    }

    /**
     * Consulta un votante por su documento.
     *
     * @param id documento a consultar
     * @return el registro si existe, vacio en caso contrario
     */
    public Optional<RegistryRecord> findVoter(int id) {
        try {
            return repo.findById(id);
        } catch (Exception e) {
            throw new IllegalStateException("Error de persistencia: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todos los votantes registrados.
     *
     * @return lista (posiblemente vacia) de registros
     */
    public List<RegistryRecord> listVoters() {
        try {
            return repo.findAll();
        } catch (Exception e) {
            throw new IllegalStateException("Error de persistencia: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un votante por su documento.
     *
     * @param id documento a eliminar
     * @return {@code true} si existia y se elimino, {@code false} si no existia
     */
    public boolean removeVoter(int id) {
        try {
            return repo.deleteById(id);
        } catch (Exception e) {
            throw new IllegalStateException("Error de persistencia: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula las estadisticas agregadas del padron.
     *
     * @return total, edad promedio, minima y maxima (ceros si no hay registros)
     */
    public Stats getStats() {
        try {
            List<RegistryRecord> all = repo.findAll();
            if (all.isEmpty()) {
                return new Stats(0, 0.0, 0, 0);
            }
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            long sum = 0;
            for (RegistryRecord r : all) {
                int age = r.getAge();
                sum += age;
                min = Math.min(min, age);
                max = Math.max(max, age);
            }
            double avg = (double) sum / all.size();
            return new Stats(all.size(), avg, min, max);
        } catch (Exception e) {
            throw new IllegalStateException("Error de persistencia: " + e.getMessage(), e);
        }
    }
}
