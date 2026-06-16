package edu.unisabana.proyecto.unit;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.domain.model.Gender;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.RegisterResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Pruebas <b>unitarias</b> de las reglas de negocio del caso de uso {@link Registry}.
 *
 * <p>Cubren las validaciones que se resuelven <i>antes</i> de tocar la persistencia,
 * por lo que el repositorio no debe ser invocado. Se usa un mock de Mockito solo
 * para satisfacer la dependencia; se verifica que no haya interacciones con el.</p>
 *
 * <p>Patron <b>AAA</b> (Arrange - Act - Assert) en cada prueba.</p>
 */
public class RegistryUnitTest {

    private RegistryRepositoryPort repo;
    private Registry registry;

    @Before
    public void setUp() {
        repo = mock(RegistryRepositoryPort.class);
        registry = new Registry(repo);
    }

    @Test
    public void shouldReturnInvalidWhenPersonIsNull() {
        // Arrange / Act
        RegisterResult result = registry.registerVoter(null);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
        verifyNoInteractions(repo);
    }

    @Test
    public void shouldReturnInvalidWhenIdIsZeroOrNegative() {
        // Arrange
        Person zero = new Person("Cero", 0, 25, Gender.MALE, true);
        Person negative = new Person("Negativo", -3, 25, Gender.MALE, true);

        // Act / Assert
        assertEquals(RegisterResult.INVALID, registry.registerVoter(zero));
        assertEquals(RegisterResult.INVALID, registry.registerVoter(negative));
        verifyNoInteractions(repo);
    }

    @Test
    public void shouldReturnDeadWhenPersonIsNotAlive() {
        // Arrange
        Person dead = new Person("Difunto", 10, 40, Gender.MALE, false);

        // Act
        RegisterResult result = registry.registerVoter(dead);

        // Assert
        assertEquals(RegisterResult.DEAD, result);
        verifyNoInteractions(repo);
    }

    @Test
    public void shouldReturnUnderageWhenAgeBelow18() {
        // Arrange
        Person underage = new Person("Menor", 11, 17, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(underage);

        // Assert
        assertEquals(RegisterResult.UNDERAGE, result);
        verifyNoInteractions(repo);
    }
}
