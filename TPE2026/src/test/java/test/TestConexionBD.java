package test;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Pruebas de integracion de la conexion y la desconexion de BaseDeDatos con MySQL real.
 * Cubre los bloques "Conexion a la base de datos" y "Desconexion" del Excel de caja negra.
 * <p>
 * Las clases erroneas de la conexion (servidor caido, credenciales incorrectas) no se
 * automatizan porque URL, usuario y contrasena son constantes de BaseDeDatos: se prueban
 * a mano cambiando la constante y comprobando que getConexion() queda nula.
 */
public class TestConexionBD extends FixtureBaseDeDatos {

	/**
	 * Clase correcta (servidor activo y credenciales validas): la conexion se abre al
	 * obtener la instancia, no es nula y esta lista para usarse (isValid).
	 */
	@Test
	public void testConexion_Valida() throws Exception {
		Assert.assertNotNull("La conexion no se abrio.", bd.getConexion());
		Assert.assertTrue("La conexion no es valida.", bd.getConexion().isValid(2));
		Assert.assertFalse("La conexion deberia estar abierta.", bd.getConexion().isClosed());
	}

	/**
	 * Clase correcta de desconexion (conexion abierta): cerrarConexion() deja la conexion
	 * cerrada.
	 */
	@Test
	public void testDesconexion_ConexionAbierta() throws Exception {
		bd.cerrarConexion();
		Assert.assertTrue("La conexion deberia estar cerrada.", bd.getConexion().isClosed());
	}

	/**
	 * Clase erronea de desconexion (conexion ya cerrada): cerrar por segunda vez no debe
	 * lanzar excepcion y la conexion sigue cerrada.
	 */
	@Test
	public void testDesconexion_ConexionYaCerrada() throws Exception {
		bd.cerrarConexion();
		bd.cerrarConexion();
		Assert.assertTrue("La conexion deberia seguir cerrada.", bd.getConexion().isClosed());
	}

	/**
	 * Clase erronea de desconexion: operar contra la base luego de cerrar la conexion debe
	 * lanzar SQLException (el error de SQL se propaga, no se oculta). Este error lo genera el
	 * driver, no el servidor MySQL, por eso no tiene codigo de MySQL: se verifica el SQLState
	 * estandar 08003 (conexion cerrada).
	 */
	@Test
	public void testDesconexion_OperarConConexionCerrada() throws Exception {
		dao.cerrarConexion();
		try {
			dao.cargarBD();
			Assert.fail("Se esperaba SQLException al operar con la conexion cerrada.");
		} catch (SQLException e) {
			Assert.assertEquals("Se esperaba error de conexion cerrada.", SQLSTATE_CONEXION_CERRADA, e.getSQLState());
		}
	}
}
