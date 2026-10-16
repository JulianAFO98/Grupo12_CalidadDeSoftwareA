package test;

import org.junit.Assert;
import org.junit.Test;

/** Pruebas de DataAccessObject.eliminar(dni) (bloque "eliminar(dni)" del Excel de caja negra). */
public class TestDaoEliminar extends FixtureBaseDeDatos {

	/** Clase 1 (DNI existente): se elimina la fila, quedan 19 y el DNI ya no esta en la tabla. */
	@Test
	public void testEliminar_DniExistente() throws Exception {
		dao.eliminar("12345678");
		Assert.assertEquals(CANTIDAD_SEMILLA - 1, contarAsociados());
		Assert.assertNull("El asociado no fue eliminado.", filaPorDni("12345678"));
	}

	/** Clase 2 (DNI inexistente): no falla y la tabla queda sin cambios (20 filas). */
	@Test
	public void testEliminar_DniInexistente() throws Exception {
		dao.eliminar("00000000");
		Assert.assertEquals(CANTIDAD_SEMILLA, contarAsociados());
	}

}
