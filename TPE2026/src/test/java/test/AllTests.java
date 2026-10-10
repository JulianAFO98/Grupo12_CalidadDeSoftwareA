package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TestAsociadoDTO.class,
	TestConexionBD.class,
	TestSingletonBD.class,
	TestInicializacionBD.class,
	TestUnicidadDNI.class,
	TestDaoGuardar.class,
	TestDaoCargar.class,
	TestDaoEliminar.class,
	TestDaoActualizar.class,
	TestAmbulanciaMock.class,
	TestAmbulanciaEstados.class,
	TestAmbulanciaConcurrencia.class,
	TestRetornoAutomatico.class
})
public class AllTests {

}
