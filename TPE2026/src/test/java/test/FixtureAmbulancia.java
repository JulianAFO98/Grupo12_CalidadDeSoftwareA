package test;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import modelo.modeloDominio.ambulancia.Ambulancia;
import modelo.modeloDominio.ambulancia.State;
import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.personas.operario.Operario;
import modelo.modeloDominio.util.Domicilio;

/**
 * Fixture base de las pruebas de Ambulancia, State y RetornoAutomatico.
 * <p>
 * Cada prueba recibe una Ambulancia nueva (con su DisponibleState real) y un doble de
 * State (mock) listo para instalar con {@link #instalarMock()}. Tambien ofrece utilidades
 * para pruebas con hilos: lanzar hilos, esperar a que queden dormidos en wait() y esperar
 * una condicion con limite de tiempo. Al terminar cada prueba, termina todos los hilos que
 * se hayan lanzado para que no queden colgados y afecten a la siguiente.
 * <p>
 * Todas las pruebas tienen un limite de {@value #LIMITE_SEGUNDOS} segundos: si un hilo queda
 * bloqueado para siempre (deadlock o notificacion perdida) la prueba falla en vez de colgarse.
 */
public abstract class FixtureAmbulancia {

	private static final int LIMITE_SEGUNDOS = 15;
	private static final long ESPERA_MAXIMA_MS = 3000;

	/** Corta cualquier prueba que tarde mas de LIMITE_SEGUNDOS (por ejemplo, por un deadlock). */
	@Rule
	public Timeout limite = Timeout.seconds(LIMITE_SEGUNDOS);

	protected Ambulancia ambulancia;
	/** Doble de State: no hace nada y registra las llamadas que recibe. */
	protected State estadoMock;

	private final List<Thread> hilos = new ArrayList<>();

	@Before
	public void setUpAmbulancia() {
		ambulancia = new Ambulancia();
		estadoMock = mock(State.class);
	}

	/** Termina todos los hilos lanzados por la prueba (despertandolos e interrumpiendolos). */
	@After
	public void tearDownAmbulancia() throws Exception {
		ambulancia.setSimulacionActiva(false);
		for (Thread t : hilos) {
			t.interrupt();
		}
		for (Thread t : hilos) {
			t.join(1000);
		}
	}

	/** Reemplaza el estado real de la ambulancia por el doble (mock). */
	protected void instalarMock() {
		ambulancia.setState(estadoMock);
	}

	/** Asociado valido, sin ambulancia propia. */
	protected Asociado crearAsociado() {
		return new Asociado("Ana", "Test", "99999999", "2235000000",
				new Domicilio("Calle Test", 100, "Mar del Plata"));
	}

	/** Operario valido asociado a la ambulancia de la prueba. */
	protected Operario crearOperario() {
		return new Operario("Omar", "Test", "11111111", "2235000001",
				new Domicilio("Calle Test", 200, "Mar del Plata"), ambulancia);
	}

	/** Crea un hilo con la tarea indicada, lo registra para su limpieza y lo arranca. */
	protected Thread lanzarHilo(Runnable tarea) {
		Thread t = new Thread(tarea);
		registrar(t);
		t.start();
		return t;
	}

	/** Registra un hilo creado por la prueba para que se termine al finalizar. */
	protected void registrar(Thread t) {
		t.setDaemon(true);
		hilos.add(t);
	}

	/** Espera (con limite de tiempo) a que el hilo quede en el estado indicado, ej: WAITING. */
	protected void esperarEstadoDelHilo(Thread t, Thread.State esperado) throws Exception {
		esperarHasta("el hilo no llego al estado " + esperado, () -> t.getState() == esperado);
	}

	/** Espera (con limite de tiempo) a que el hilo termine y falla si sigue vivo. */
	protected void esperarFinDelHilo(Thread t) throws Exception {
		t.join(ESPERA_MAXIMA_MS);
		Assert.assertFalse("El hilo no termino: posible deadlock o hilo colgado.", t.isAlive());
	}

	/** Espera (con limite de tiempo) a que se cumpla la condicion; si no, falla con el mensaje. */
	protected void esperarHasta(String mensaje, BooleanSupplier condicion) throws Exception {
		long limiteAbsoluto = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
		while (!condicion.getAsBoolean()) {
			if (System.currentTimeMillis() > limiteAbsoluto) {
				throw new TimeoutException(mensaje);
			}
			Thread.sleep(10);
		}
	}
}
