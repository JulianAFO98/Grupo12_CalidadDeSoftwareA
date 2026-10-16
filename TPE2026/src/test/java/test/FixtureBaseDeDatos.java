package test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.util.Domicilio;
import persistencia.BaseDeDatos;
import persistencia.DataAccessObject;

/**
 * Fixture base de las pruebas de integracion contra MySQL.
 * <p>
 * Garantiza que <b>cada</b> prueba arranque desde el mismo estado conocido (base
 * <code>Grupo_7</code> con la tabla <code>Asociados</code> y los 20 datos semilla) y que no
 * deje rastros para la siguiente, de modo que las pruebas sean independientes entre si y se
 * puedan ejecutar en cualquier orden.
 * <p>
 * Ademas ofrece utilidades comunes (consultas directas a la base, armado de asociados y
 * reinicio del Singleton) para que las clases de prueba concretas solo contengan la logica
 * de cada caso.
 */
public abstract class FixtureBaseDeDatos {

	/** Cantidad de registros que inserta iniciarBD() como datos semilla. */
	protected static final int CANTIDAD_SEMILLA = 20;

	/*
	 * Codigos de error de MySQL (SQLException.getErrorCode()). Estan documentados por MySQL y
	 * no dependen del idioma ni de la version, a diferencia del texto del mensaje.
	 */
	/** Clave duplicada: se viola una restriccion UNIQUE (ej: DNI repetido). */
	protected static final int ER_DUP_ENTRY = 1062;
	/** El dato es mas largo que la columna (ej: DNI de 11 caracteres en VARCHAR(10)). */
	protected static final int ER_DATA_TOO_LONG = 1406;
	/** La tabla no existe. */
	protected static final int ER_NO_SUCH_TABLE = 1146;
	/** No hay una base de datos seleccionada (ej: se elimino la base en uso). */
	protected static final int ER_NO_DB_ERROR = 1046;
	/** SQLState estandar de "conexion inexistente o cerrada" (lo genera el driver, no MySQL). */
	protected static final String SQLSTATE_CONEXION_CERRADA = "08003";

	protected BaseDeDatos bd;
	protected DataAccessObject dao;

	/**
	 * Prepara el escenario: obtiene la instancia de BaseDeDatos (reiniciandola si una prueba
	 * anterior dejo la conexion cerrada), restaura la base a su estado semilla y crea el DAO.
	 */
	@Before
	public void setUpBD() throws Exception {
		asegurarConexionAbierta();
		bd = BaseDeDatos.getInstancia();
		bd.iniciarBD();
		dao = new DataAccessObject();
	}

	/**
	 * Limpia el escenario: deja la base nuevamente en su estado semilla y con la conexion
	 * abierta, aunque la prueba haya eliminado tablas o cerrado la conexion.
	 */
	@After
	public void tearDownBD() throws Exception {
		asegurarConexionAbierta();
		BaseDeDatos.getInstancia().iniciarBD();
	}

	/**
	 * Si la instancia Singleton quedo con la conexion cerrada, la descarta para que
	 * getInstancia() cree una nueva (y vuelva a abrir la conexion).
	 */
	protected void asegurarConexionAbierta() throws Exception {
		Connection c = obtenerInstanciaActual() == null ? null : obtenerInstanciaActual().getConexion();
		if (c == null || c.isClosed()) {
			reiniciarSingleton();
		}
	}

	/**
	 * Pone en null el atributo estatico "instancia" de BaseDeDatos (por reflexion), para poder
	 * volver a construir el Singleton.
	 */
	protected void reiniciarSingleton() throws Exception {
		Field f = BaseDeDatos.class.getDeclaredField("instancia");
		f.setAccessible(true);
		f.set(null, null);
	}

	private BaseDeDatos obtenerInstanciaActual() throws Exception {
		Field f = BaseDeDatos.class.getDeclaredField("instancia");
		f.setAccessible(true);
		return (BaseDeDatos) f.get(null);
	}

	/**
	 * Verifica que la SQLException se haya producido por el motivo esperado, comparando su
	 * codigo de error de MySQL.
	 */
	protected void assertCodigoError(String mensaje, int codigoEsperado, SQLException e) {
		Assert.assertEquals(mensaje, codigoEsperado, e.getErrorCode());
	}

	/** Ejecuta una sentencia SQL directa (DDL o DML), sin pasar por el DAO. */
	protected void ejecutarSQL(String sql) throws SQLException {
		try (Statement st = bd.getConexion().createStatement()) {
			st.execute(sql);
		}
	}

	/** Devuelve el primer valor de la primera fila de una consulta (ej: un COUNT). */
	protected String escalar(String sql) throws SQLException {
		try (Statement st = bd.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
			return rs.next() ? rs.getString(1) : null;
		}
	}

	/** Cantidad de filas de la tabla Asociados, consultada directamente a la base. */
	protected int contarAsociados() throws SQLException {
		return Integer.parseInt(escalar("SELECT COUNT(*) FROM Asociados"));
	}

	/** Devuelve la fila (columna -> valor) que cumple la consulta, o null si no hay ninguna. */
	protected Map<String, String> fila(String sql) throws SQLException {
		try (Statement st = bd.getConexion().createStatement(); ResultSet rs = st.executeQuery(sql)) {
			if (!rs.next()) {
				return null;
			}
			ResultSetMetaData md = rs.getMetaData();
			Map<String, String> fila = new LinkedHashMap<>();
			for (int i = 1; i <= md.getColumnCount(); i++) {
				fila.put(md.getColumnLabel(i).toLowerCase(), rs.getString(i));
			}
			return fila;
		}
	}

	/** Fila de la tabla Asociados con el DNI indicado (consulta directa), o null. */
	protected Map<String, String> filaPorDni(String dni) throws SQLException {
		return fila("SELECT * FROM Asociados WHERE dni = '" + dni + "'");
	}

	/** Fila de la tabla Asociados con el id indicado (consulta directa), o null. */
	protected Map<String, String> filaPorId(int id) throws SQLException {
		return fila("SELECT * FROM Asociados WHERE id = " + id);
	}

	/** Arma un Asociado de dominio con los datos indicados (sin ambulancia asociada). */
	protected Asociado crearAsociado(String nombre, String apellido, String dni, String telefono,
			String calle, int numero, String ciudad) {
		return new Asociado(nombre, apellido, dni, telefono, new Domicilio(calle, numero, ciudad));
	}

	/** Asociado valido de uso general, con el DNI indicado (no existe en los datos semilla). */
	protected Asociado asociadoConDni(String dni) {
		return crearAsociado("Ana", "Test", dni, "2235000000", "Calle Test", 100, "Mar del Plata");
	}
}
