package test;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import persistencia.AsociadoDTO;

/**
 * Pruebas de las operaciones de lectura del DataAccessObject: cargar(id), cargarConLimite()
 * y cargarBD() (bloques homonimos del Excel de caja negra).
 */
public class TestDaoCargar extends FixtureBaseDeDatos {

	// ---------- cargar(id) ----------

	/**
	 * Clase 1 (valor limite id = 1): devuelve el primer asociado semilla, Juan State.
	 * Si la operacion lanza una excepcion, se reporta como fallo (no como error).
	 */
	@Test
	public void testCargar_PrimerId() throws Exception {
		AsociadoDTO a = null;
		try {
			a = dao.cargar(1);
		} catch (SQLException e) {
			Assert.fail("Fallo de manera inesperada por un error de SQL cuando debia devolver el asociado con id 1.");
		} catch (Exception e) {
			Assert.fail("Fallo de manera inesperada cuando debia devolver el asociado con id 1.");
		}
		Assert.assertNotNull("No se encontro el asociado 1.", a);
		Assert.assertEquals("Juan", a.getNombre());
		Assert.assertEquals("State", a.getApellido());
		Assert.assertEquals("12345678", a.getDni());
	}

	/**
	 * Clase 1 (valor limite id = 20): devuelve el ultimo asociado semilla, Adrian Composite.
	 * Si la operacion lanza una excepcion, se reporta como fallo (no como error).
	 */
	@Test
	public void testCargar_UltimoId() throws Exception {
		AsociadoDTO a = null;
		try {
			a = dao.cargar(20);
		} catch (SQLException e) {
			Assert.fail("Fallo de manera inesperada por un error de SQL cuando debia devolver el asociado con id 20.");
		} catch (Exception e) {
			Assert.fail("Fallo de manera inesperada cuando debia devolver el asociado con id 20.");
		}
		Assert.assertNotNull("No se encontro el asociado 20.", a);
		Assert.assertEquals("Adrian", a.getNombre());
		Assert.assertEquals("Composite", a.getApellido());
		Assert.assertEquals("28293031", a.getDni());
	}

	/**
	 * Clase 1 (valor intermedio id = 10): devuelve David Adapter.
	 * Si la operacion lanza una excepcion, se reporta como fallo (no como error).
	 */
	@Test
	public void testCargar_IdIntermedio() throws Exception {
		AsociadoDTO a = null;
		try {
			a = dao.cargar(10);
		} catch (SQLException e) {
			Assert.fail("Fallo de manera inesperada por un error de SQL cuando debia devolver el asociado con id 10.");
		} catch (Exception e) {
			Assert.fail("Fallo de manera inesperada cuando debia devolver el asociado con id 10.");
		}
		Assert.assertNotNull("No se encontro el asociado 10.", a);
		Assert.assertEquals("David", a.getNombre());
		Assert.assertEquals("Adapter", a.getApellido());
	}

	/**
	 * Clase 2 (id inexistente, 21): no hay asociado con ese id, el resultado es null.
	 * Si la operacion lanza una excepcion, se reporta como fallo (no como error).
	 */
	@Test
	public void testCargar_IdInexistente() throws Exception {
		AsociadoDTO resultado = new AsociadoDTO();
		try {
			resultado = dao.cargar(21);
		} catch (SQLException e) {
			Assert.fail("Fallo de manera inesperada por un error de SQL cuando debia devolver null porque el asociado con id 21 no existe.");
		} catch (Exception e) {
			Assert.fail("Fallo de manera inesperada cuando debia devolver null porque el asociado con id 21 no existe.");
		}
		Assert.assertNull("No deberia existir el asociado 21.", resultado);
	}

	// ---------- cargarConLimite(limite) ----------

	/** Clase 1 (valor limite 1): devuelve exactamente 1 asociado. */
	@Test
	public void testCargarConLimite_Uno() throws Exception {
		Assert.assertEquals(1, dao.cargarConLimite(1).size());
	}

	/** Clase 1 (valor intermedio 5): devuelve exactamente 5 asociados. */
	@Test
	public void testCargarConLimite_Cinco() throws Exception {
		Assert.assertEquals(5, dao.cargarConLimite(5).size());
	}

	/** Clase 1 (valor limite 20): devuelve los 20 asociados existentes. */
	@Test
	public void testCargarConLimite_IgualALaCantidad() throws Exception {
		Assert.assertEquals(CANTIDAD_SEMILLA, dao.cargarConLimite(20).size());
	}

	/** Clase 2 (limite mayor a la cantidad, 25): devuelve solo los 20 existentes. */
	@Test
	public void testCargarConLimite_MayorALaCantidad() throws Exception {
		Assert.assertEquals(CANTIDAD_SEMILLA, dao.cargarConLimite(25).size());
	}

	// ---------- cargarBD() ----------

	/** Clase 1 (tabla con registros): devuelve los 20 asociados semilla. */
	@Test
	public void testCargarBD_ConRegistros() throws Exception {
		ArrayList<AsociadoDTO> lista = dao.cargarBD();
		Assert.assertEquals(CANTIDAD_SEMILLA, lista.size());
	}

	/** Clase 2 (tabla vacia): se borran todos los asociados y devuelve una lista vacia. */
	@Test
	public void testCargarBD_TablaVacia() throws Exception {
		ejecutarSQL("DELETE FROM Asociados");
		Assert.assertTrue("La lista deberia estar vacia.", dao.cargarBD().isEmpty());
	}
}
