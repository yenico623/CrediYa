# Sistema Financiero - CrediYa S.A.S.

Sistema de control de préstamos y cobros desarrollado en Java para la gestión de empleados, clientes, préstamos y pagos, utilizando una arquitectura modular basada en el modelo DAO, MVC y persistencia en base de datos MySQL mediante JDBC.

## Autor y Repositorio
* **Desarrolladora:** Yenifer Yurley Cortez Montañez
* **Usuario de GitHub:** [yenico623](https://github.com/yenico623)

---

## Descripcion General del Proyecto
CrediYa S.A.S. es un sistema diseñado para digitalizar la gestión de créditos personales que anteriormente se manejaba en hojas de cálculo. La aplicación opera desde la consola y ofrece tres portales de acceso independientes según el rol del usuario (Administrador, Empleado y Cliente), garantizando la seguridad, el control de accesos y la integridad transaccional de los datos financieros.

---

## Funcionalidades Principales

### 1. Portal Operativo (Administrador)
* **Gestion de Empleados:** Permite listar a los empleados registrados y eliminarlos del sistema de forma segura mediante validación por ID.
* **Reporte General de Clientes:** Muestra la lista completa de clientes registrados con sus datos de contacto (nombre, documento, correo y teléfono).
* **Reporte General de Empleados:** Consulta la información de los asesores activos junto con sus respectivos salarios.
* **Reporte General de Prestamos:** Detalla todos los créditos otorgados en el sistema indicando cliente, monto inicial, saldo capital e estado actual.
* **Reporte de Clientes Morosos:** Identifica y lista a los clientes que presentan deudas o inactividad prolongada en sus pagos.

### 2. Portal Operativo (Empleado / Asesor)
* **Modulo de Clientes:** Permite registrar nuevos clientes en el sistema asociándolos a cuentas de usuario y credenciales de acceso automatizadas.
* **Modulo de Prestamos:** 
  * Otorga nuevos créditos seleccionando al cliente de la base de datos.
  * Aplica de forma automática el cálculo de intereses mediante el patrón **Strategy** (Interés Simple para créditos de 6 cuotas o menos; Interés Compuesto para más de 6 cuotas).
  * Calcula y muestra en pantalla de manera automática el valor de la cuota mensual.
* **Modulo de Pagos:**
  * Registra abonos y pagos sobre los préstamos activos.
  * Afecta automáticamente el saldo capital pendiente y actualiza el estado de las cuotas correspondientes en el plan de pagos.
  * Muestra un histórico detallado de los pagos realizados en el sistema.

### 3. Portal de Clientes
* **Inicio de Sesion:** Acceso directo mediante el número de documento del cliente.
* **Informacion Personal:** Visualización de datos básicos de perfil (nombre, documento, correo, teléfono y fecha de nacimiento).
* **Mis Prestamos:** Consulta de los créditos activos asociados a su cuenta, mostrando el monto solicitado, saldo pendiente, estado.

---

## Estructura de la Base de Datos (MySQL)

El sistema opera sobre una base de datos relacional llamada `crediya_db` conformada por las siguientes tablas principales:

* **`usuarios`**: Almacena las credenciales de acceso, nombre de usuario, contraseña y el rol (`ADMIN`, `EMPLEADO`, `CLIENTE`).
* **`personas`**: Contiene la información demográfica compartida (nombre, documento, correo, teléfono, fecha de nacimiento) vinculada a un usuario.
* **`empleados`**: Extiende la información de personas para los asesores de la empresa, incluyendo su salario.
* **`clientes`**: Relaciona a las personas que actúan como clientes dentro del sistema financiero.
* **`prestamos`**: Registra los créditos otorgados, asociando al cliente y al empleado, el monto, la tasa de interés, el número de cuotas, fechas de solicitud/aprobación y el estado (`ACTIVO`, `PAGADO`, etc.).
* **`cuotas_plan`**: Define el cronograma o plan de cuotas de cada préstamo con sus fechas de vencimiento, capital esperado, intereses y estados individuales.
* **`pagos`**: Almacena el registro histórico de los abonos realizados a los préstamos, método de pago, referencia, monto y fecha.
* **`detalles_distribucion_pago`**: Detalla cómo se distribuyó económicamente cada pago entre capital e intereses respecto a las cuotas correspondientes.

---

## Patrones de Diseño Aplicados
1. **Patrón Singleton:** Implementado en la clase de conexión a la base de datos (`dbConnection`) para garantizar una única instancia de conexión activa durante toda la ejecución del programa.
2. **Patrón Strategy:** Aplicado en el cálculo de intereses (`InteresSimple` e `InteresCompuesto`) para seleccionar de forma dinámica la lógica financiera según el plazo del préstamo.
3. **Patrón DAO (Data Access Object):g** Separación estricta de la lógica de acceso a datos (`ClienteDao`, `PrestamoDao`, `PagoDao`, `UsuarioDao`) respecto a las interfaces visuales de consola.

---

## Tecnologías Utilizadas
* **Lenguaje:** Java
* **Base de Datos:** MySQL
* **Conectividad:** JDBC (Java Database Connectivity)
* **Control de Versiones:** Git y GitHub (`yenico623`)
