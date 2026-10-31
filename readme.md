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
