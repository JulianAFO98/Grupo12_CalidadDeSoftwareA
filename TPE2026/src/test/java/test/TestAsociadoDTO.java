package test;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import persistencia.AsociadoDTO;

public class TestAsociadoDTO {
	
	private AsociadoDTO asociadoDTO;
	
	@Before
	public void setUp() throws Exception {
		this.asociadoDTO = new AsociadoDTO();
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testSetNombre() {
		asociadoDTO.setNombre("Juan");
		Assert.assertEquals("El nombre no ha sido registrado correctamente.", "Juan", asociadoDTO.getNombre());
	}
	
	
	@Test
	public void testSetNombre_Vacio() {
		asociadoDTO.setNombre("");
		Assert.assertEquals("El nombre no ha sido registrado correctamente.", "", asociadoDTO.getNombre());
	}
	
	
	@Test
	public void testSetApellido() {
		asociadoDTO.setApellido("Fernandez");
		Assert.assertEquals("El apellido no ha sido registrado correctamente.", "Fernandez", asociadoDTO.getApellido());
	}
	
	@Test
	public void testSetApellido_Vacio() {
		asociadoDTO.setApellido("");
		Assert.assertEquals("El apellido no ha sido registrado correctamente.", "", asociadoDTO.getApellido());
	}
	 
	
	@Test
	public void testSetDni() {
		asociadoDTO.setDni("42.100.000");
		Assert.assertEquals("El numero de dni no ha sido registrado correctamente.", "42.100.000", asociadoDTO.getDni());
	}
	
	@Test
	public void testSetDni_Vacio() {
		asociadoDTO.setDni("");
		Assert.assertEquals("El numero de dni no ha sido registrado correctamente.", "", asociadoDTO.getDni());
	}
	
	
	@Test
	public void testSetCiudad() {
		asociadoDTO.setCiudad("Pinamar");
		Assert.assertEquals("La ciudad no ha sido registrada correctamente.", "Pinamar", asociadoDTO.getCiudad());
	}
	
	@Test
	public void testSetCiudad_Vacio() {
		asociadoDTO.setCiudad("");
		Assert.assertEquals("La ciudad no ha sido registrada correctamente.", "", asociadoDTO.getCiudad());
	}
	
	
	@Test
	public void testSetCalle() {
		asociadoDTO.setCalle("Av. Colon");
		Assert.assertEquals("La calle no ha sido registrada correctamente.", "Av. Colon", asociadoDTO.getCalle());
	}
	
	@Test
	public void testSetCalle_Vacio() {
		asociadoDTO.setCalle("");
		Assert.assertEquals("La calle no ha sido registrada correctamente.", "", asociadoDTO.getCalle());
	}
	
	
	@Test
	public void testSetNumero() {
		asociadoDTO.setNumero(2500);
		Assert.assertEquals("El numero de la vivienda no ha sido registrado correctamente",2500, asociadoDTO.getNumero());
	}
	
	@Test
	public void testSetNumero_ValorLimite() {
		asociadoDTO.setNumero(0);
		Assert.assertEquals("El numero de la vivienda no ha sido registrado correctamente",0, asociadoDTO.getNumero());
	}
	
	
	@Test
	public void testSetTelefono() {
		asociadoDTO.setTelefono("0022 123456");
		Assert.assertEquals("El numero de telefono no ha sido registrado correctamente.", "0022 123456", asociadoDTO.getTelefono());
	}
	
	@Test
	public void testSetTelefono_Vacio() {
		asociadoDTO.setTelefono("");
		Assert.assertEquals("El numero de telefono no ha sido registrado correctamente.", "", asociadoDTO.getTelefono());
	}
}
