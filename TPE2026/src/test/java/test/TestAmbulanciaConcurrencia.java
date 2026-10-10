package test;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.personas.asociado.Asociado;
import modelo.modeloDominio.personas.operario.Operario;

/**
 * Pruebas de concurrencia de Ambulancia: varios hilos compiten por la misma ambulancia.
 * Verifican la exclusion mutua (metodos synchronized), la espera con wait(), el despertar con
 * notifyAll() y la reaccion a la finalizacion de la simulacion y a la interrupcion.
 * Corresponde a los bloques de concurrencia del Excel de caja negra.
 * <p>
 * Tecnica: los hilos se arrancan juntos con un CountDownLatch, se comprueba su estado con
 * Thread.getState() (WAITING = dormido en wait()) y toda espera tiene limite de tiempo, por lo
 * que un deadlock hace fallar la prueba en lugar de colgarla. No se usan sleeps largos.
 */
public class TestAmbulanciaConcurrencia extends FixtureAmbulancia {

	private final AtomicInteger dentro = new AtomicInteger();
	private final AtomicInteger maximoSimultaneo = new AtomicInteger();

	@Before
	public void instalar() {
		instalarMock();
	}

	/**
	 * Configura el mock para que cada SolicitudDeAtencionDomicilio() registre cuantos hilos
	 * estan "dentro" al mismo tiempo (y dure 20 ms para dar chance a que se superpongan).
	 */
	private void medirSimultaneidad() {
		doAnswer(inv -> {
			int n = dentro.incrementAndGet();
			maximoSimultaneo.accumulateAndGet(n, Math::max);
			Thread.sleep(20);
			dentro.decrementAndGet();
			return null;
		}).when(estadoMock).SolicitudDeAtencionDomicilio();
	}

	private int hilosEnEspera(List<Thread> hilos) {
		int n = 0;
		for (Thread t : hilos) {
			if (t.getState() == Thread.State.WAITING) {
				n++;
			}
		}
		return n;
	}

	// ---------- exclusion mutua ----------

	/** Clase 1 (un solo hilo): atenderDomicilio termina y el estado recibe una invocacion. */
	@Test
	public void testExclusionMutua_UnHilo() throws Exception {
		Thread t = lanzarHilo(() -> ambulancia.atenderDomicilio(crearAsociado()));
		esperarFinDelHilo(t);
		verify(estadoMock, times(1)).SolicitudDeAtencionDomicilio();
	}

	/**
	 * Clase 2 (5 hilos a la vez, ambulancia libre): solo uno obtiene la ambulancia; los otros
	 * 4 quedan esperando (WAITING) y el estado recibe una sola invocacion.
	 */
	@Test
	public void testExclusionMutua_CincoHilosSoloUnoEntra() throws Exception {
		medirSimultaneidad();
		List<Thread> hilos = lanzarCincoAsociadosSimultaneos();

		esperarHasta("no quedaron 4 hilos esperando", () -> hilosEnEspera(hilos) == 4);

		verify(estadoMock, times(1)).SolicitudDeAtencionDomicilio();
	}

	/**
	 * Clase 2 (5 hilos, se libera la ambulancia 4 veces): los 5 hilos terminan uno tras otro,
	 * nunca hay mas de uno dentro a la vez, el estado recibe 5 invocaciones y no hay deadlock.
	 */
	@Test
	public void testExclusionMutua_CincoHilosTerminanTodos() throws Exception {
		medirSimultaneidad();
		List<Thread> hilos = lanzarCincoAsociadosSimultaneos();
		esperarHasta("no quedaron 4 hilos esperando", () -> hilosEnEspera(hilos) == 4);

		for (int liberadas = 1; liberadas <= 4; liberadas++) {
			ambulancia.regresarSinPaciente();
			verify(estadoMock, timeout(2000).times(1 + liberadas)).SolicitudDeAtencionDomicilio();
		}

		for (Thread t : hilos) {
			esperarFinDelHilo(t);
		}
		verify(estadoMock, times(5)).SolicitudDeAtencionDomicilio();
		Assert.assertEquals("Hubo mas de un hilo dentro a la vez: falla la exclusion mutua.",
				1, maximoSimultaneo.get());
	}

	/**
	 * Clase 2 (un operario y un asociado a la vez): solo uno obtiene la ambulancia primero; el
	 * otro espera y termina cuando se libera. Ambos terminan, sin deadlock.
	 */
	@Test
	public void testExclusionMutua_OperarioContraAsociado() throws Exception {
		Operario operario = crearOperario();
		Asociado asociado = crearAsociado();
		CountDownLatch salida = new CountDownLatch(1);
		Thread tOperario = lanzarHilo(() -> {
			esperar(salida);
			ambulancia.solicitarMantenimiento(operario);
		});
		Thread tAsociado = lanzarHilo(() -> {
			esperar(salida);
			ambulancia.atenderDomicilio(asociado);
		});
		salida.countDown();

		esperarHasta("ninguno de los dos quedo esperando exactamente uno",
				() -> (tOperario.getState() == Thread.State.WAITING) ^ (tAsociado.getState() == Thread.State.WAITING));
		Assert.assertEquals("Solo uno de los dos debio entrar.", 1, mockingDetails(estadoMock).getInvocations().size());

		ambulancia.regresarSinPaciente();

		esperarFinDelHilo(tOperario);
		esperarFinDelHilo(tAsociado);
		verify(estadoMock).SolicitudMantenimiento();
		verify(estadoMock).SolicitudDeAtencionDomicilio();
	}

	// ---------- espera, despertar, fin de simulacion e interrupcion ----------

	/** Clase 1: con la ambulancia ocupada, el hilo queda esperando (WAITING) y no llama al estado. */
	@Test
	public void testEspera_AmbulanciaOcupada() throws Exception {
		ambulancia.setOcupado(true);
		Thread t = lanzarHilo(() -> ambulancia.atenderDomicilio(crearAsociado()));

		esperarEstadoDelHilo(t, Thread.State.WAITING);

		verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
	}

	/**
	 * Clase 2: con el hilo esperando, otro hilo libera la ambulancia (notifyAll); el hilo se
	 * despierta, toma la ambulancia (queda ocupada de nuevo) y termina.
	 */
	@Test
	public void testDespertar_AlLiberarLaAmbulancia() throws Exception {
		ambulancia.setOcupado(true);
		Thread t = lanzarHilo(() -> ambulancia.atenderDomicilio(crearAsociado()));
		esperarEstadoDelHilo(t, Thread.State.WAITING);

		ambulancia.regresarSinPaciente();

		esperarFinDelHilo(t);
		verify(estadoMock).SolicitudDeAtencionDomicilio();
		Assert.assertTrue("El hilo despertado deberia haber ocupado la ambulancia.", ambulancia.isOcupado());
	}

	/**
	 * Clase 3: con 3 hilos esperando, al finalizar la simulacion se despiertan los 3 y terminan
	 * sin llamar nunca al estado.
	 */
	@Test
	public void testFinDeSimulacion_DespiertaATodos() throws Exception {
		ambulancia.setOcupado(true);
		List<Thread> hilos = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			hilos.add(lanzarHilo(() -> ambulancia.atenderDomicilio(crearAsociado())));
		}
		esperarHasta("no quedaron 3 hilos esperando", () -> hilosEnEspera(hilos) == 3);

		ambulancia.setSimulacionActiva(false);

		for (Thread t : hilos) {
			esperarFinDelHilo(t);
		}
		verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
	}

	/**
	 * Clase 4: al interrumpir un hilo que espera, termina; su bandera de interrupcion queda
	 * activada (se restaura) y el estado no recibe ninguna llamada.
	 */
	@Test
	public void testInterrupcion_HiloEnEspera() throws Exception {
		ambulancia.setOcupado(true);
		AtomicBoolean interrumpidoAlSalir = new AtomicBoolean(false);
		Thread t = lanzarHilo(() -> {
			ambulancia.atenderDomicilio(crearAsociado());
			interrumpidoAlSalir.set(Thread.currentThread().isInterrupted());
		});
		esperarEstadoDelHilo(t, Thread.State.WAITING);

		t.interrupt();

		esperarFinDelHilo(t);
		Assert.assertTrue("La bandera de interrupcion deberia quedar activada.", interrumpidoAlSalir.get());
		verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
	}

	// ---------- utilidades ----------

	/** Lanza 5 asociados que piden atenderDomicilio() en el mismo instante (latch de salida). */
	private List<Thread> lanzarCincoAsociadosSimultaneos() {
		CountDownLatch salida = new CountDownLatch(1);
		List<Thread> hilos = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			hilos.add(lanzarHilo(() -> {
				esperar(salida);
				ambulancia.atenderDomicilio(crearAsociado());
			}));
		}
		salida.countDown();
		return hilos;
	}

	private void esperar(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
