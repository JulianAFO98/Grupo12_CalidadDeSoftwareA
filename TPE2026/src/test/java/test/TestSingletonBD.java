package test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

import persistencia.BaseDeDatos;

/**
 * Pruebas del patron Singleton de BaseDeDatos (bloque "Singleton" del Excel de caja negra).
 */
public class TestSingletonBD extends FixtureBaseDeDatos {

	/**
	 * Primera invocacion de getInstancia() (instancia nula): crea la instancia y la devuelve
	 * no nula.
	 */
	@Test
	public void testGetInstancia_PrimeraVez() throws Exception {
		reiniciarSingleton();
		Assert.assertNotNull("getInstancia() no creo la instancia.", BaseDeDatos.getInstancia());
	}

	/**
	 * Invocaciones siguientes: dos llamadas a getInstancia() devuelven exactamente el mismo
	 * objeto (misma referencia), no dos instancias equivalentes.
	 */
	@Test
	public void testGetInstancia_MismoObjeto() {
		Assert.assertSame("Se obtuvieron dos instancias distintas.",
				BaseDeDatos.getInstancia(), BaseDeDatos.getInstancia());
	}

	/**
	 * Clase erronea (instanciacion directa): todos los constructores de BaseDeDatos son
	 * privados, por lo que nadie puede crear otra instancia con new.
	 */
	@Test
	public void testConstructor_Privado() {
		for (Constructor<?> c : BaseDeDatos.class.getDeclaredConstructors()) {
			Assert.assertTrue("El constructor no es privado: " + c, Modifier.isPrivate(c.getModifiers()));
		}
	}
}
