DROP DATABASE IF EXISTS crediya_db;
CREATE DATABASE crediya_db;
USE crediya_db;


CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    rol ENUM('ADMIN', 'EMPLEADO', 'CLIENTE') NOT NULL
);


CREATE TABLE personas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNIQUE NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    documento VARCHAR(30) UNIQUE NOT NULL,
    correo VARCHAR(80) UNIQUE,
    telefono VARCHAR(20),
    fecha_nacimiento DATE NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);


CREATE TABLE empleados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT UNIQUE NOT NULL,
    salario DECIMAL(10,2),
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE
);


CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT UNIQUE NOT NULL,
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE
);


CREATE TABLE prestamos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    empleado_id INT, 
    monto DECIMAL(12,2) NOT NULL,            
    saldo_capital DECIMAL(12,2) NOT NULL,    
    interes DECIMAL(5,2) NOT NULL,           
    cuotas INT NOT NULL,
    fecha_solicitud DATE NOT NULL,
    fecha_aprobacion DATE,
    estado ENUM('SOLICITADO', 'ASIGNADO', 'ACTIVO', 'PAGADO', 'RECHAZADO', 'EN_MORA') DEFAULT 'SOLICITADO',
    FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

CREATE TABLE cuotas_plan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT NOT NULL,
    numero_cuota INT NOT NULL,      
    fecha_vencimiento DATE NOT NULL,
    capital_esperado DECIMAL(10,2) NOT NULL,
    interes_esperado DECIMAL(10,2) NOT NULL,
    capital_pendiente DECIMAL(10,2) NOT NULL, 
    interes_pendiente DECIMAL(10,2) NOT NULL,
    mora_acumulada DECIMAL(10,2) DEFAULT 0.00,    
    estado ENUM('PENDIENTE', 'ABONADA', 'PAGADA') DEFAULT 'PENDIENTE',
    FOREIGN KEY (prestamo_id) REFERENCES prestamos(id) ON DELETE CASCADE
);


CREATE TABLE pagos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT NOT NULL,
    metodo_pago ENUM('EFECTIVO', 'TRANSFERENCIA', 'TARJETA') NOT NULL, 
    numero_referencia VARCHAR(50) NULL,
    fecha_reporte DATE NOT NULL,
    fecha_aplicacion DATETIME NULL,         
    monto DECIMAL(10,2) NOT NULL,            
    estado ENUM('EN_REVISION', 'APROBADO', 'ERROR') DEFAULT 'EN_REVISION', 
    empleado_revisor_id INT, 
    FOREIGN KEY (prestamo_id) REFERENCES prestamos(id),
    FOREIGN KEY (empleado_revisor_id) REFERENCES empleados(id)
);

CREATE TABLE detalles_distribucion_pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pago_id INT NOT NULL,
    cuota_id INT NOT NULL,
    monto_a_mora DECIMAL(10,2) DEFAULT 0.00,
    monto_a_interes DECIMAL(10,2) DEFAULT 0.00,
    monto_a_capital DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (pago_id) REFERENCES pagos(id) ON DELETE CASCADE,
    FOREIGN KEY (cuota_id) REFERENCES cuotas_plan(id)
);


INSERT INTO usuarios (username, password, rol) VALUES ('admin', 'admin123', 'ADMIN');
