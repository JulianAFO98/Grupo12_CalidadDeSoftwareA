package test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import modelo.modeloDominio.personas.asociado.Asociado;

/**
 * Pruebas de DataAccessObject.actualizar(asociado) (bloque "actualizar(asociado)" del Excel).
 * El asociado se identifica por su DNI: solo debe modificarse la fila de ese DNI.
 */
public class TestDaoActualizar extends FixtureBaseDeDatos {

	/**
	 * Clase 1: se cambia el telefono del asociado de DNI 87654321 (id 2). Debe cambiar solo
	 * esa fila; el asociado id 1 debe quedar intacto.
	 */
	@Test
	public void testActualizar_ModificaSoloElAsociadoIndicado() throws Exception {
		Map<String, String> primeroAntes = filaPorId(1);
		Asociado a = crearAsociado("Maria", "Template", "87654321", "2230000000", "Libertad", 742, "Mar del Plata");

		dao.actualizar(a);

		Assert.assertEquals("No se actualizo el telefono del asociado indicado.",
				"2230000000", filaPorDni("87654321").get("telefono"));
		Assert.assertEquals("Se modifico un asociado que no era el indicado.", primeroAntes, filaPorId(1));
	}

	/** Clase 1: se cambia la calle del asociado de DNI 12345678 (id 1) y la fila refleja el cambio. */
	@Test
	public void testActualizar_CambiaCalle() throws Exception {
		Asociado a = crearAsociado("Juan", "State", "12345678", "2235551234", "Calle Nueva", 123, "Mar del Plata");

		dao.actualizar(a);

		Assert.assertEquals("Calle Nueva", filaPorDni("12345678").get("calle"));
		Assert.assertEquals("La cantidad de filas no debe cambiar.", CANTIDAD_SEMILLA, contarAsociados());
	}

	/** Clase 2 (asociado nulo): la precondicion falla con AssertionError. */
	@Test(expected = AssertionError.class)
	public void testActualizar_Null() throws Exception {
		dao.actualizar(null);
	}
}
