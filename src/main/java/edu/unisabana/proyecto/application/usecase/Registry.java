package edu.unisabana.proyecto.application.usecase;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.RegisterResult;

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
}
