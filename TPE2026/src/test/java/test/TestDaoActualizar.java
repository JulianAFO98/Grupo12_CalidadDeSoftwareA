package test;

import java.sql.SQLException;
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
	 * esa fila; el asociado id 1 debe quedar intacto. Si la operacion lanza una excepcion, se
	 * reporta como fallo (no como error).
	 */
	@Test
	public void testActualizar_ModificaSoloElAsociadoIndicado() throws Exception {
		Map<String, String> primeroAntes = filaPorId(1);
		Asociado a = crearAsociado("Maria", "Template", "87654321", "2230000000", "Libertad", 742, "Mar del Plata");

		try {
			dao.actualizar(a);
		} catch (SQLException e) {
			Assert.fail("Fallo de manera inesperada por un error de SQL cuando debia actualizar el telefono del asociado con DNI 87654321.");
		} catch (Exception e) {
			Assert.fail("Fallo de manera inesperada cuando debia actualizar el telefono del asociado con DNI 87654321.");
		}

		Assert.assertEquals("No se actualizo el telefono del asociado indicado.",
				"2230000000", filaPorDni("87654321").get("telefono"));
		Assert.assertEquals("Se modifico un asociado que no era el indicado.", primeroAntes, filaPorId(1));
	}

	/**
	 * Clase 1: se cambia la calle del asociado de DNI 10112233 (id 15, Valeria Iterator). La fila
	 * de ese DNI debe reflejar el cambio, el asociado id 1 debe quedar intacto y la cantidad de
	 * filas no cambia. Se usa un asociado distinto del id 1 a proposito: con el id 1 el test
	 * pasaria aunque el UPDATE tuviera el id fijo. Si la operacion lanza una excepcion, se
	 * reporta como fallo (no como error).
	 */
	@Test
	public void testActualizar_CambiaCalle() throws Exception {
		Map<String, String> primeroAntes = filaPorId(1);
		Asociado a = crearAsociado("Valeria", "Iterator", "10112233", "2235223344", "Calle Nueva", 850, "Mar del Plata");

		try {
			dao.actualizar(a);
		} catch (SQLException e) {
			Assert.fail("Fallo de manera inesperada por un error de SQL cuando debia actualizar la calle del asociado con DNI 10112233.");
		} catch (Exception e) {
			Assert.fail("Fallo de manera inesperada cuando debia actualizar la calle del asociado con DNI 10112233.");
		}

		Assert.assertEquals("No se actualizo la calle del asociado indicado.",
				"Calle Nueva", filaPorDni("10112233").get("calle"));
		Assert.assertEquals("Se modifico un asociado que no era el indicado.", primeroAntes, filaPorId(1));
		Assert.assertEquals("La cantidad de filas no debe cambiar.", CANTIDAD_SEMILLA, contarAsociados());
	}

}
