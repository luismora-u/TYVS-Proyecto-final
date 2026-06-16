package edu.unisabana.proyecto.integration;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.domain.model.Gender;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.RegisterResult;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRecord;
import edu.unisabana.proyecto.infrastructure.persistence.RegistryRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pruebas de <b>integracion</b> entre el caso de uso {@link Registry} y el
 * adaptador real de persistencia {@link RegistryRepository}, usando una base de
 * datos <b>H2 en memoria</b>.
 *
 * <p>Verifican la colaboracion real entre capas (servicio &harr; repositorio):
 * que los datos se persistan, que se respete la unicidad de documento y que la
 * lectura posterior recupere exactamente lo guardado.</p>
 *
 * <p>Patron <b>AAA</b> y aislamiento por prueba mediante {@code deleteAll()}.</p>
 */
public class RegistryH2IntegrationTest {

    private static final String JDBC_URL = "jdbc:h2:mem:itdb;DB_CLOSE_DELAY=-1";

    private RegistryRepositoryPort repo;
    private Registry registry;

    @Before
    public void setUp() throws Exception {
        repo = new RegistryRepository(JDBC_URL);
        repo.initSchema();   // Arrange: crear tabla
        repo.deleteAll();    // Arrange: aislar de pruebas previas
        registry = new Registry(repo);
    }

    @Test
    public void shouldPersistValidVoter() throws Exception {
        // Arrange
        Person ana = new Person("Ana", 100, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(ana);

        // Assert
        assertEquals(RegisterResult.VALID, result);
        assertTrue(repo.existsById(100));
    }

    @Test
    public void shouldRejectDuplicatedDocument() throws Exception {
        // Arrange
        Person first = new Person("Ana", 100, 30, Gender.FEMALE, true);
        Person sameId = new Person("Ana clon", 100, 45, Gender.FEMALE, true);

        // Act
        RegisterResult firstResult = registry.registerVoter(first);
        RegisterResult secondResult = registry.registerVoter(sameId);

        // Assert
        assertEquals(RegisterResult.VALID, firstResult);
        assertEquals(RegisterResult.DUPLICATED, secondResult);
    }

    @Test
    public void shouldReadBackPersistedData() throws Exception {
        // Arrange
        Person carlos = new Person("Carlos", 200, 50, Gender.MALE, true);

        // Act
        registry.registerVoter(carlos);
        Optional<RegistryRecord> stored = repo.findById(200);

        // Assert
        assertTrue(stored.isPresent());
        assertEquals(200, stored.get().getId());
        assertEquals("Carlos", stored.get().getName());
        assertEquals(50, stored.get().getAge());
        assertTrue(stored.get().isAlive());
    }

    @Test
    public void shouldNotPersistUnderagePerson() throws Exception {
        // Arrange
        Person teen = new Person("Menor", 300, 16, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(teen);

        // Assert
        assertEquals(RegisterResult.UNDERAGE, result);
        assertFalse(repo.existsById(300));
    }
}
