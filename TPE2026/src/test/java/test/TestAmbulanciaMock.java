package test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.RetornoAutomatico;
import modelo.modeloDominio.ambulancia.State;

/**
 * Pruebas atomicas de Ambulancia usando un doble de State (mock de Mockito).
 * <p>
 * El mock reemplaza al estado real: no hace nada y registra las llamadas que recibe. Asi se
 * prueba que Ambulancia delegue cada operacion en el metodo correcto de State, sin depender
 * de ninguna implementacion concreta de estado. Corresponde a los bloques de "mock de State"
 * del Excel de caja negra (hoja "Mocks y Concurrencia").
 */
public class TestAmbulanciaMock extends FixtureAmbulancia {

	@Before
	public void instalar() {
		instalarMock();
	}

	// ---------- setState ----------

	/** Clase 1: setState con un estado valido lo deja como estado actual de la ambulancia. */
	@Test
	public void testSetState_Valido() {
		Assert.assertSame("El estado actual deberia ser el mock instalado.", estadoMock, ambulancia.getEstadoActual());
	}

	// ---------- solicitarMantenimiento(operario) ----------

	/**
	 * Clases 1, 3 y 5 (operario valido, ambulancia libre, simulacion activa): el mock recibe
	 * SolicitudMantenimiento() una sola vez y la ambulancia queda ocupada.
	 */
	@Test
	public void testSolicitarMantenimiento_Valido() {
		ambulancia.solicitarMantenimiento(crearOperario());
		verify(estadoMock).SolicitudMantenimiento();
		Assert.assertTrue("La ambulancia deberia quedar ocupada.", ambulancia.isOcupado());
	}

	/**
	 * Clase 4 (ambulancia ocupada): el operario queda esperando y no se llama al estado;
	 * al liberar la ambulancia, continua y el estado recibe SolicitudMantenimiento().
	 */
	@Test
	public void testSolicitarMantenimiento_AmbulanciaOcupada() throws Exception {
		verificarEsperaConAmbulanciaOcupada(() -> ambulancia.solicitarMantenimiento(crearOperario()),
				s -> s.SolicitudMantenimiento());
	}

	/** Clase 6 (simulacion finalizada): retorna sin esperar, sin llamar al estado y sin ocuparla. */
	@Test
	public void testSolicitarMantenimiento_SimulacionFinalizada() {
		ambulancia.setSimulacionActiva(false);
		ambulancia.solicitarMantenimiento(crearOperario());
		verify(estadoMock, never()).SolicitudMantenimiento();
		Assert.assertFalse(ambulancia.isOcupado());
	}

	// ---------- atenderDomicilio(asociado) ----------

	/** Clases 1, 3 y 5: el mock recibe SolicitudDeAtencionDomicilio() una vez y queda ocupada. */
	@Test
	public void testAtenderDomicilio_Valido() {
		ambulancia.atenderDomicilio(crearAsociado());
		verify(estadoMock).SolicitudDeAtencionDomicilio();
		Assert.assertTrue(ambulancia.isOcupado());
	}

	/** Clase 4 (ambulancia ocupada): el asociado espera; al liberarla, el estado recibe la llamada. */
	@Test
	public void testAtenderDomicilio_AmbulanciaOcupada() throws Exception {
		verificarEsperaConAmbulanciaOcupada(() -> ambulancia.atenderDomicilio(crearAsociado()),
				s -> s.SolicitudDeAtencionDomicilio());
	}

	/** Clase 6 (simulacion finalizada): retorna sin llamar al estado. */
	@Test
	public void testAtenderDomicilio_SimulacionFinalizada() {
		ambulancia.setSimulacionActiva(false);
		ambulancia.atenderDomicilio(crearAsociado());
		verify(estadoMock, never()).SolicitudDeAtencionDomicilio();
		Assert.assertFalse(ambulancia.isOcupado());
	}

	// ---------- trasladarALaClinica(asociado) ----------

	/** Clases 1, 3 y 5: el mock recibe SolicitudDeTraslado() una vez y queda ocupada. */
	@Test
	public void testTrasladarALaClinica_Valido() {
		ambulancia.trasladarALaClinica(crearAsociado());
		verify(estadoMock).SolicitudDeTraslado();
		Assert.assertTrue(ambulancia.isOcupado());
	}

	/** Clase 4 (ambulancia ocupada): el asociado espera; al liberarla, el estado recibe la llamada. */
	@Test
	public void testTrasladarALaClinica_AmbulanciaOcupada() throws Exception {
		verificarEsperaConAmbulanciaOcupada(() -> ambulancia.trasladarALaClinica(crearAsociado()),
				s -> s.SolicitudDeTraslado());
	}

	/** Clase 6 (simulacion finalizada): retorna sin llamar al estado. */
	@Test
	public void testTrasladarALaClinica_SimulacionFinalizada() {
		ambulancia.setSimulacionActiva(false);
		ambulancia.trasladarALaClinica(crearAsociado());
		verify(estadoMock, never()).SolicitudDeTraslado();
		Assert.assertFalse(ambulancia.isOcupado());
	}

	// ---------- retornos y fin de mantenimiento ----------

	/** retornoAutomatico delega en el estado: el mock recibe RetornoClinica() una sola vez. */
	@Test
	public void testRetornoAutomatico_DelegaEnElEstado() {
		ambulancia.retornoAutomatico(new RetornoAutomatico(ambulancia));
		verify(estadoMock).RetornoClinica();
	}

	/** regresarSinPaciente delega en el estado (RetornoClinica) y libera la ambulancia. */
	@Test
	public void testRegresarSinPaciente_DelegaYLibera() {
		ambulancia.setOcupado(true);
		ambulancia.regresarSinPaciente();
		verify(estadoMock).RetornoClinica();
		Assert.assertFalse("La ambulancia deberia quedar libre.", ambulancia.isOcupado());
	}

	/** volviendoDelTaller delega en el estado (SolicitudMantenimiento) y libera la ambulancia. */
	@Test
	public void testVolviendoDelTaller_DelegaYLibera() {
		ambulancia.setOcupado(true);
		ambulancia.volviendoDelTaller();
		verify(estadoMock).SolicitudMantenimiento();
		Assert.assertFalse(ambulancia.isOcupado());
	}

	// ---------- utilidad comun ----------

	/**
	 * Con la ambulancia ocupada, la operacion (ejecutada en otro hilo) debe quedar esperando
	 * (WAITING) sin llamar al estado. Al liberar la ambulancia con regresarSinPaciente(), el hilo
	 * continua, termina y el estado recibe la llamada esperada una vez.
	 *
	 * @param operacion     la llamada a Ambulancia que se prueba
	 * @param llamadaEstado el metodo de State que deberia recibir el mock
	 */
	private void verificarEsperaConAmbulanciaOcupada(Runnable operacion, Consumer<State> llamadaEstado) throws Exception {
		ambulancia.setOcupado(true);
		Thread hilo = lanzarHilo(operacion);

		esperarEstadoDelHilo(hilo, Thread.State.WAITING);
		llamadaEstado.accept(verify(estadoMock, never()));

		ambulancia.regresarSinPaciente();

		esperarFinDelHilo(hilo);
		llamadaEstado.accept(verify(estadoMock, timeout(2000)));
	}
}
