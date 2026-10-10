package test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import modelo.modeloDominio.personas.asociado.Asociado;

/** Pruebas de DataAccessObject.guardar() (bloque "guardar()" del Excel de caja negra). */
public class TestDaoGuardar extends FixtureBaseDeDatos {

	/**
	 * Clase 1 (asociado valido): se inserta una fila nueva (21 en total) y todos los datos
	 * guardados coinciden con los del asociado enviado.
	 */
	@Test
	public void testGuardar_AsociadoValido() throws Exception {
		Asociado a = crearAsociado("Ana", "Test", "99999999", "2235000000", "Calle Test", 100, "Mar del Plata");
		dao.guardar(a);

		Assert.assertEquals("No se agrego una fila.", CANTIDAD_SEMILLA + 1, contarAsociados());
		Map<String, String> f = filaPorDni("99999999");
		Assert.assertNotNull("No se encontro el asociado guardado.", f);
		Assert.assertEquals("Ana", f.get("nombre"));
		Assert.assertEquals("Test", f.get("apellido"));
		Assert.assertEquals("2235000000", f.get("telefono"));
		Assert.assertEquals("Calle Test", f.get("calle"));
		Assert.assertEquals("100", f.get("numero"));
		Assert.assertEquals("Mar del Plata", f.get("ciudad"));
	}

	/** Clase 2 (asociado nulo): la precondicion falla con AssertionError y no se inserta nada. */
	@Test(expected = AssertionError.class)
	public void testGuardar_Null() throws Exception {
		dao.guardar(null);
	}
}
