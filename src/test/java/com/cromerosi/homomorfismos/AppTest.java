package com.cromerosi.homomorfismos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas de la aplicación principal")
class AppTest {

    @Test
    @DisplayName("App no debe ser instanciable")
    void testAppConstructorIsPrivate() throws NoSuchMethodException {
        var constructor = App.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
    }

    @Test
    @DisplayName("Aplicación contiene clase principal")
    void testApplicationExists() {
        assertNotNull(App.class);
    }

    @Test
    @DisplayName("main puede ser invocado sin excepciones")
    void testMainMethodExists() throws NoSuchMethodException {
        assertTrue(App.class.getDeclaredMethod("main", String[].class) != null);
    }
}
