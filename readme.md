# Proyecto de testing - Calidad de Software

Trabajo práctico final de Calidad de Software: estrategia de testing sobre **Clínica**, una aplicación Java que gestiona los datos de los asociados en una base MySQL y simula el servicio de una ambulancia con hilos concurrentes.

Las pruebas se diseñaron con técnicas de caja negra (clases de equivalencia y valores límite) y se implementaron con JUnit 4 y Mockito.

## Integrantes

- Pezzi, Esteban
- Fernandez Ordoqui, Julian Agustin
- Beltran, Jennifer
- Teruel López, Leonel

## Consigna

1. **Testing unitario de `AsociadoDTO`:** pruebas aisladas de la asignación, recuperación y validaciones de los setters, sin servicios externos.
2. **Testing de integración con base de datos:** pruebas de `AsociadoDTO`, `DataAccessObject` y `BaseDeDatos` contra MySQL real, que verifican el Singleton de la conexión, la inicialización de tablas y datos semilla, y las operaciones CRUD.
3. **Testing de integración con mocks y concurrencia:** pruebas de `Ambulancia`, la interfaz `State` y `RetornoAutomatico`, con dobles de prueba para los estados y evaluación de la máquina de estados, los recursos compartidos y los hilos.

La entrega incluye el código de las suites, capturas de las pruebas en verde y en rojo, y un informe con el diseño de los casos de prueba, los resultados y las conclusiones.

## Documentación

### [Informe de testing Clínica](Informe%20-%20TP%20Clinica.pdf)

Proceso de testing, fallos encontrados en cada nivel, conclusiones y resultados de la ejecución.

### [Pruebas de caja negra](Tablas%20de%20Caja%20Negra%20-%20TP%20Clinica%20.xlsx)

Diseño de los casos de prueba con clases de equivalencia y valores límite, en tres hojas (Setters, Integración, y Mocks y Concurrencia). Cada hoja contiene:

- **Escenarios:** estado inicial del sistema para cada caso.
- **Tabla de partición:** condiciones de cada método con sus clases válidas e inválidas.
- **Batería de pruebas:** casos con tipo de clase, valores de entrada, salida esperada, clases cubiertas y escenario.
