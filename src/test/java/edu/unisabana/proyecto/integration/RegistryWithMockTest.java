package edu.unisabana.proyecto.integration;

import edu.unisabana.proyecto.application.port.out.RegistryRepositoryPort;
import edu.unisabana.proyecto.application.usecase.Registry;
import edu.unisabana.proyecto.domain.model.Gender;
import edu.unisabana.proyecto.domain.model.Person;
import edu.unisabana.proyecto.domain.model.RegisterResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de <b>integracion con dobles de prueba (Mockito)</b> del caso de uso
 * {@link Registry}.
 *
 * <p>Aislan la logica de aplicacion de la infraestructura simulando el
 * comportamiento del puerto {@link RegistryRepositoryPort} y verificando la
 * colaboracion esperada con el (patrones {@code when()}, {@code verify()},
 * {@code never()}).</p>
 */
public class RegistryWithMockTest {

    private RegistryRepositoryPort repo;
    private Registry registry;

    @Before
    public void setUp() {
        repo = mock(RegistryRepositoryPort.class);
        registry = new Registry(repo);
    }

    /**
     * Given el repositorio indica que el documento ya existe;
     * When intento registrar la persona;
     * Then el resultado es DUPLICATED y nunca se invoca save().
     */
    @Test
    public void shouldReturnDuplicatedWhenRepoSaysExists() throws Exception {
        // Arrange
        when(repo.existsById(7)).thenReturn(true);
        Person p = new Person("Ana", 7, 25, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert
        assertEquals(RegisterResult.DUPLICATED, result);
        verify(repo, never()).save(anyInt(), anyString(), anyInt(), anyBoolean());
    }

    /**
     * Given el repositorio indica que el documento no existe;
     * When intento registrar la persona;
     * Then el resultado es VALID y se invoca save() exactamente una vez.
     */
    @Test
    public void shouldSaveAndReturnValidWhenRepoSaysNotExists() throws Exception {
        // Arrange
        when(repo.existsById(9)).thenReturn(false);
        Person p = new Person("Beto", 9, 33, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert
        assertEquals(RegisterResult.VALID, result);
        verify(repo, times(1)).save(9, "Beto", 33, true);
    }

    /**
     * Given el repositorio lanza una excepcion al consultar;
     * When intento registrar la persona;
     * Then la excepcion se traduce a un IllegalStateException controlado.
     */
    @Test(expected = IllegalStateException.class)
    public void shouldWrapRepositoryFailure() throws Exception {
        // Arrange
        when(repo.existsById(5)).thenThrow(new RuntimeException("fallo de BD"));
        Person p = new Person("Caro", 5, 40, Gender.FEMALE, true);

        // Act -> debe propagar IllegalStateException
        registry.registerVoter(p);
    }
}
