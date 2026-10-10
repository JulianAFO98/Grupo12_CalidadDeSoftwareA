package test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.RetornoAutomatico;

/**
 * Pruebas del hilo RetornoAutomatico, que periodicamente (cada 10 s) pide a la ambulancia que
 * vuelva a la clinica mientras la simulacion este activa. Corresponde al bloque
 * "RetornoAutomatico (hilo)" del Excel de caja negra.
 * <p>
 * El intervalo es una constante privada de 10 s, asi que no se espera a que dispare: se
 * comprueba el CICLO DE VIDA del hilo (si sigue vivo o termina) con plazos cortos.
 */
public class TestRetornoAutomatico extends FixtureAmbulancia {

	private Thread arrancarRetorno() {
		Thread t = new Thread(new RetornoAutomatico(ambulancia));
		registrar(t);
		t.start();
		return t;
	}

	/**
	 * Clase 1 (simulacion activa): el hilo debe seguir vivo, durmiendo hasta su proximo
	 * intento, 500 ms despues de arrancar.
	 */
	@Test
	public void testSimulacionActiva_ElHiloSigueVivo() throws Exception {
		Thread t = arrancarRetorno();
		Thread.sleep(500);
		Assert.assertTrue("El hilo deberia seguir vivo mientras la simulacion esta activa.", t.isAlive());
	}

	/**
	 * Clase 3 (interrupcion): al interrumpir el hilo termina limpiamente en menos de 1 s.
	 * Nota: con la condicion invertida del bucle (defecto) este test pasa porque el hilo ya
	 * termino solo; el defecto lo detecta el test anterior.
	 */
	@Test
	public void testInterrupcion_TerminaLimpio() throws Exception {
		Thread t = arrancarRetorno();
		Thread.sleep(200);
		t.interrupt();
		t.join(1000);
		Assert.assertFalse("El hilo deberia terminar al ser interrumpido.", t.isAlive());
	}

	/**
	 * Clase 2 (simulacion finalizada antes de arrancar): el hilo debe terminar de inmediato
	 * (menos de 1 s) sin pedir ningun retorno a la ambulancia.
	 */
	@Test
	public void testSimulacionFinalizada_ElHiloTermina() throws Exception {
		instalarMock();
		ambulancia.setSimulacionActiva(false);
		Thread t = arrancarRetorno();
		t.join(1000);
		Assert.assertFalse("El hilo deberia terminar si la simulacion ya finalizo.", t.isAlive());
		verify(estadoMock, never()).RetornoClinica();
	}
}
