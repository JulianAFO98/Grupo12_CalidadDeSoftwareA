package test;

import org.junit.Assert;
import org.junit.Test;

import modelo.modeloDominio.ambulancia.AtendiendoEnDomicilioState;
import modelo.modeloDominio.ambulancia.DisponibleState;
import modelo.modeloDominio.ambulancia.EnTallerState;
import modelo.modeloDominio.ambulancia.RegresandoClinicaSinPacienteState;
import modelo.modeloDominio.ambulancia.RegresandoDelTallerState;
import modelo.modeloDominio.ambulancia.State;
import modelo.modeloDominio.ambulancia.TrasladandoPacienteClinicaState;

/**
 * Pruebas de integracion de la maquina de estados: Ambulancia acoplada a los estados
 * REALES (sin mocks). Cada prueba lleva la ambulancia a un estado, dispara un evento y
 * comprueba el estado resultante. Corresponde al bloque "Transiciones de la maquina de
 * estados" del Excel de caja negra.
 * <p>
 * Caminos usados para llegar a cada estado, partiendo de Disponible:
 * Trasladando = SolicitudDeTraslado; Atendiendo = SolicitudDeAtencionDomicilio;
 * RegresandoSinPaciente = Atendiendo + RetornoClinica; EnTaller = SolicitudMantenimiento;
 * RegresandoDelTaller = EnTaller + SolicitudMantenimiento.
 */
public class TestAmbulanciaEstados extends FixtureAmbulancia {

	private State estado() {
		return ambulancia.getEstadoActual();
	}

	private void assertEstado(Class<? extends State> esperado) {
		Assert.assertEquals("Estado actual incorrecto.", esperado, estado().getClass());
	}

	private void irATrasladando() {
		estado().SolicitudDeTraslado();
	}

	private void irAAtendiendo() {
		estado().SolicitudDeAtencionDomicilio();
	}

	private void irARegresandoSinPaciente() {
		irAAtendiendo();
		estado().RetornoClinica();
	}

	private void irAEnTaller() {
		estado().SolicitudMantenimiento();
	}

	private void irARegresandoDelTaller() {
		irAEnTaller();
		estado().SolicitudMantenimiento();
	}

	// ---------- Disponible ----------

	/** Disponible + SolicitudDeTraslado() -> Trasladando paciente. */
	@Test
	public void testDisponible_Traslado() {
		estado().SolicitudDeTraslado();
		assertEstado(TrasladandoPacienteClinicaState.class);
	}

	/** Disponible + SolicitudDeAtencionDomicilio() -> Atendiendo en domicilio. */
	@Test
	public void testDisponible_Domicilio() {
		estado().SolicitudDeAtencionDomicilio();
		assertEstado(AtendiendoEnDomicilioState.class);
	}

	/** Disponible + SolicitudMantenimiento() -> En taller. */
	@Test
	public void testDisponible_Mantenimiento() {
		estado().SolicitudMantenimiento();
		assertEstado(EnTallerState.class);
	}

	/** Disponible + RetornoClinica() -> sigue Disponible (ya esta en la clinica). */
	@Test
	public void testDisponible_Retorno() {
		estado().RetornoClinica();
		assertEstado(DisponibleState.class);
	}

	// ---------- Trasladando paciente ----------

	/**
	 * Trasladando + RetornoClinica() -> Disponible (segun el Javadoc de la clase: "Cambia el
	 * estado de la ambulancia a DisponibleState"). Tarda 1 s por un sleep interno.
	 */
	@Test
	public void testTrasladando_Retorno() {
		irATrasladando();
		estado().RetornoClinica();
		assertEstado(DisponibleState.class);
	}

	/** Trasladando + SolicitudDeTraslado() -> sigue Trasladando (no puede atender otro). */
	@Test
	public void testTrasladando_Traslado() {
		irATrasladando();
		estado().SolicitudDeTraslado();
		assertEstado(TrasladandoPacienteClinicaState.class);
	}

	// ---------- Atendiendo en domicilio ----------

	/** Atendiendo + RetornoClinica() -> Regresando a la clinica sin paciente. */
	@Test
	public void testAtendiendo_Retorno() {
		irAAtendiendo();
		estado().RetornoClinica();
		assertEstado(RegresandoClinicaSinPacienteState.class);
	}

	/** Atendiendo + SolicitudMantenimiento() -> sigue Atendiendo (no puede atender). */
	@Test
	public void testAtendiendo_Mantenimiento() {
		irAAtendiendo();
		estado().SolicitudMantenimiento();
		assertEstado(AtendiendoEnDomicilioState.class);
	}

	// ---------- Regresando a la clinica sin paciente ----------

	/** RegresandoSinPaciente + RetornoClinica() -> Disponible. */
	@Test
	public void testRegresandoSinPaciente_Retorno() {
		irARegresandoSinPaciente();
		estado().RetornoClinica();
		assertEstado(DisponibleState.class);
	}

	/** RegresandoSinPaciente + SolicitudDeTraslado() -> Trasladando paciente. */
	@Test
	public void testRegresandoSinPaciente_Traslado() {
		irARegresandoSinPaciente();
		estado().SolicitudDeTraslado();
		assertEstado(TrasladandoPacienteClinicaState.class);
	}

	/** RegresandoSinPaciente + SolicitudDeAtencionDomicilio() -> Atendiendo en domicilio. */
	@Test
	public void testRegresandoSinPaciente_Domicilio() {
		irARegresandoSinPaciente();
		estado().SolicitudDeAtencionDomicilio();
		assertEstado(AtendiendoEnDomicilioState.class);
	}

	// ---------- En taller ----------

	/** EnTaller + SolicitudMantenimiento() -> Regresando del taller. */
	@Test
	public void testEnTaller_Mantenimiento() {
		irAEnTaller();
		estado().SolicitudMantenimiento();
		assertEstado(RegresandoDelTallerState.class);
	}

	/** EnTaller + RetornoClinica() -> sigue En taller (no puede retornar desde el taller). */
	@Test
	public void testEnTaller_Retorno() {
		irAEnTaller();
		estado().RetornoClinica();
		assertEstado(EnTallerState.class);
	}

	// ---------- Regresando del taller ----------

	/** RegresandoDelTaller + RetornoClinica() -> Disponible. */
	@Test
	public void testRegresandoDelTaller_Retorno() {
		irARegresandoDelTaller();
		estado().RetornoClinica();
		assertEstado(DisponibleState.class);
	}

	/** RegresandoDelTaller + SolicitudMantenimiento() -> sigue Regresando del taller. */
	@Test
	public void testRegresandoDelTaller_Mantenimiento() {
		irARegresandoDelTaller();
		estado().SolicitudMantenimiento();
		assertEstado(RegresandoDelTallerState.class);
	}
}
