package test;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Pruebas de la inicializacion automatica (iniciarBD): existencia de la base y la tabla y
 * datos semilla. Bloque "Inicializacion y datos semilla" del Excel de caja negra.
 */
public class TestInicializacionBD extends FixtureBaseDeDatos {

	/** Clase 1: la base Grupo_7 existe luego de la inicializacion. */
	@Test
	public void testBaseCreada() throws Exception {
		String n = escalar("SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'Grupo_7'");
		Assert.assertEquals("La base Grupo_7 no fue creada.", "1", n);
	}

	/** Clase 3: la tabla Asociados existe dentro de Grupo_7 luego de la inicializacion. */
	@Test
	public void testTablaCreada() throws Exception {
		String n = escalar("SELECT COUNT(*) FROM information_schema.TABLES "
				+ "WHERE TABLE_SCHEMA = 'Grupo_7' AND TABLE_NAME = 'Asociados'");
		Assert.assertEquals("La tabla Asociados no fue creada.", "1", n);
	}

	/** Clase 5 (valor 20): la tabla contiene exactamente los 20 registros semilla. */
	@Test
	public void testCantidadDatosSemilla() throws Exception {
		Assert.assertEquals("Cantidad de datos semilla incorrecta.", CANTIDAD_SEMILLA, contarAsociados());
	}

	/** Clase 5 (valor limite): el primer registro semilla (id 1) tiene los datos esperados. */
	@Test
	public void testPrimerDatoSemilla() throws Exception {
		Map<String, String> f = filaPorId(1);
		Assert.assertNotNull("No existe el registro con id 1.", f);
		Assert.assertEquals("Juan", f.get("nombre"));
		Assert.assertEquals("State", f.get("apellido"));
		Assert.assertEquals("12345678", f.get("dni"));
		Assert.assertEquals("Buenos Aires", f.get("calle"));
		Assert.assertEquals("123", f.get("numero"));
		Assert.assertEquals("2235551234", f.get("telefono"));
	}

	/** Clase 5 (valor limite): el ultimo registro semilla (id 20) tiene los datos esperados. */
	@Test
	public void testUltimoDatoSemilla() throws Exception {
		Map<String, String> f = filaPorId(20);
		Assert.assertNotNull("No existe el registro con id 20.", f);
		Assert.assertEquals("Adrian", f.get("nombre"));
		Assert.assertEquals("Composite", f.get("apellido"));
		Assert.assertEquals("28293031", f.get("dni"));
		Assert.assertEquals("Olavarria", f.get("calle"));
		Assert.assertEquals("2700", f.get("numero"));
		Assert.assertEquals("2235778899", f.get("telefono"));
	}

	/**
	 * Clase 5: volver a ejecutar iniciarBD() luego de modificar los datos recrea la tabla y
	 * restaura los 20 registros semilla.
	 */
	@Test
	public void testReinicializacionRestauraSemilla() throws Exception {
		dao.guardar(asociadoConDni("99999999"));
		Assert.assertEquals(CANTIDAD_SEMILLA + 1, contarAsociados());
		bd.iniciarBD();
		Assert.assertEquals("iniciarBD() no restauro los datos semilla.", CANTIDAD_SEMILLA, contarAsociados());
	}

	/**
	 * Clase 2 (la base no existe): luego de eliminar Grupo_7, una operacion del DAO lanza
	 * SQLException. El fixture restaura la base al finalizar.
	 */
	@Test(expected = SQLException.class)
	public void testOperarSinBase() throws Exception {
		ejecutarSQL("DROP DATABASE Grupo_7");
		dao.cargarBD();
	}

	/**
	 * Clase 4 (la tabla no existe): luego de eliminar Asociados, una operacion del DAO lanza
	 * SQLException. El fixture restaura la tabla al finalizar.
	 */
	@Test(expected = SQLException.class)
	public void testOperarSinTabla() throws Exception {
		ejecutarSQL("DROP TABLE Asociados");
		dao.cargarBD();
	}

	/**
	 * Clase 6 (control): si se inserta un asociado sin reiniciar la base, la cantidad deja
	 * de ser 20. Demuestra que el conteo de la clase 5 realmente discrimina.
	 */
	@Test
	public void testCantidadDistintaDeSemilla() throws Exception {
		dao.guardar(asociadoConDni("99999999"));
		Assert.assertNotEquals(CANTIDAD_SEMILLA, contarAsociados());
	}
}
