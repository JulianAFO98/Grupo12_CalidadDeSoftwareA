package test;

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

	/** Clase 1 (valor limite id = 1): devuelve el primer asociado semilla, Juan State. */
	@Test
	public void testCargar_PrimerId() throws Exception {
		AsociadoDTO a = dao.cargar(1);
		Assert.assertNotNull("No se encontro el asociado 1.", a);
		Assert.assertEquals("Juan", a.getNombre());
		Assert.assertEquals("State", a.getApellido());
		Assert.assertEquals("12345678", a.getDni());
	}

	/** Clase 1 (valor limite id = 20): devuelve el ultimo asociado semilla, Adrian Composite. */
	@Test
	public void testCargar_UltimoId() throws Exception {
		AsociadoDTO a = dao.cargar(20);
		Assert.assertNotNull("No se encontro el asociado 20.", a);
		Assert.assertEquals("Adrian", a.getNombre());
		Assert.assertEquals("Composite", a.getApellido());
		Assert.assertEquals("28293031", a.getDni());
	}

	/** Clase 1 (valor intermedio id = 10): devuelve David Adapter. */
	@Test
	public void testCargar_IdIntermedio() throws Exception {
		AsociadoDTO a = dao.cargar(10);
		Assert.assertNotNull("No se encontro el asociado 10.", a);
		Assert.assertEquals("David", a.getNombre());
		Assert.assertEquals("Adapter", a.getApellido());
	}

	/** Clase 2 (id inexistente, 21): no hay asociado con ese id, el resultado es null. */
	@Test
	public void testCargar_IdInexistente() throws Exception {
		Assert.assertNull("No deberia existir el asociado 21.", dao.cargar(21));
	}

	/** Clase 3 (valor limite id = 0): la precondicion id > 0 falla con AssertionError. */
	@Test(expected = AssertionError.class)
	public void testCargar_IdCero() throws Exception {
		dao.cargar(0);
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

	/** Clase 3 (valor limite 0): la precondicion limite > 0 falla con AssertionError. */
	@Test(expected = AssertionError.class)
	public void testCargarConLimite_Cero() throws Exception {
		dao.cargarConLimite(0);
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
