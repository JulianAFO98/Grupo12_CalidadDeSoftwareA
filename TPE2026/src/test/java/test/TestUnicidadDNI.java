package test;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Pruebas de la unicidad del DNI (identificador unico segun el SRS 3.1),
 * garantizada por la
 * restriccion UNIQUE de la tabla. Bloque "Unicidad de DNI" del Excel de caja
 * negra.
 * La regla no se puede verificar en AsociadoDTO aislado: requiere la base de
 * datos.
 */
public class TestUnicidadDNI extends FixtureBaseDeDatos {

	/**
	 * Clase 1: guardar un asociado con un DNI que no existe lo registra (21 filas).
	 */
	@Test
	public void testGuardar_DniNuevo() throws Exception {
		dao.guardar(asociadoConDni("99999999"));
		Assert.assertEquals(CANTIDAD_SEMILLA + 1, contarAsociados());
		Assert.assertNotNull("El asociado no quedo registrado.", filaPorDni("99999999"));
	}

	/**
	 * Clases 1 y 3 (valor limite 10): un DNI de exactamente 10 caracteres se
	 * acepta.
	 */
	@Test
	public void testGuardar_DniDe10Caracteres() throws Exception {
		dao.guardar(asociadoConDni("1234567890"));
		Assert.assertNotNull("El DNI de 10 caracteres no fue registrado.", filaPorDni("1234567890"));
	}

	/** Clase 1: actualizar un asociado con un DNI que no existe cambia su DNI. */
	@Test
	public void testActualizar_DniNuevo() throws Exception {
		dao.actualizar(asociadoConDni("99999998"));
		Assert.assertNotNull("El DNI no fue actualizado.", filaPorDni("99999998"));
	}

	/**
	 * Clase 2: guardar un asociado cuyo DNI ya existe (12345678, dato semilla)
	 * lanza
	 * SQLException por clave duplicada (codigo MySQL 1062) y no agrega filas.
	 */
	@Test
	public void testGuardar_DniDuplicado() throws Exception {
		try {
			dao.guardar(asociadoConDni("12345678"));
			Assert.fail("Se esperaba SQLException por DNI duplicado.");
		} catch (SQLException e) {
			assertCodigoError("Se esperaba error de DNI duplicado.", ER_DUP_ENTRY, e);
			Assert.assertEquals("No debio agregarse ninguna fila.", CANTIDAD_SEMILLA, contarAsociados());
		}
	}

	/**
	 * Clase 2: actualizar un asociado con un DNI que ya pertenece a otro (87654321)
	 * lanza
	 * SQLException por clave duplicada (codigo MySQL 1062) y deja los datos como estaban.
	 */
	@Test
	public void testActualizar_DniDeOtroAsociado() throws Exception {
		Map<String, String> antes = filaPorId(1);
		try {
			dao.actualizar(asociadoConDni("87654321"));
			Assert.fail("Se esperaba SQLException por DNI duplicado.");
		} catch (SQLException e) {
			assertCodigoError("Se esperaba error de DNI duplicado.", ER_DUP_ENTRY, e);
			Assert.assertEquals("La fila no debio modificarse.", antes, filaPorId(1));
		}
	}

	/**
	 * Clase 4 (valor limite 11): un DNI de 11 caracteres excede el VARCHAR(10) y la
	 * base lo
	 * rechaza con SQLException por dato demasiado largo (codigo MySQL 1406; requiere modo SQL
	 * estricto).
	 */
	@Test
	public void testGuardar_DniDe11Caracteres() throws Exception {
		try {
			dao.guardar(asociadoConDni("12345678901"));
			Assert.fail("Se esperaba SQLException: el DNI excede los 10 caracteres.");
		} catch (SQLException e) {
			assertCodigoError("Se esperaba error de dato demasiado largo.", ER_DATA_TOO_LONG, e);
			Assert.assertNull("No debio guardarse el DNI.", filaPorDni("12345678901"));
		}
	}
}
