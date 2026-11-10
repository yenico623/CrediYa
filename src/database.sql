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
    proxima_fecha_pago DATE DEFAULT NULL,
    estado ENUM('SOLICITADO', 'APROBADO', 'ACTIVO', 'PAGADO', 'RECHAZADO', 'EN_MORA') DEFAULT 'SOLICITADO',
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

INSERT INTO usuarios (username, password, rol) VALUES ('carlos.emp', '1234', 'EMPLEADO');
INSERT INTO personas (usuario_id, nombre, documento, correo, telefono, fecha_nacimiento) 
VALUES (LAST_INSERT_ID(), 'Carlos Perez', '10987654', 'carlos@crediya.com', '3112223344', '1990-05-12');
INSERT INTO empleados (persona_id, salario) VALUES (LAST_INSERT_ID(), 2500000.00);

INSERT INTO usuarios (username, password, rol) VALUES ('maria.cli', '1234', 'CLIENTE');
INSERT INTO personas (usuario_id, nombre, documento, correo, telefono, fecha_nacimiento) 
VALUES (LAST_INSERT_ID(), 'Maria Duarte', '63554433', 'maria@gmail.com', '3154445566', '1995-08-20');
INSERT INTO clientes (persona_id) VALUES (LAST_INSERT_ID());

INSERT INTO usuarios (username, password, rol) VALUES ('andres.cli', '1234', 'CLIENTE');
INSERT INTO personas (usuario_id, nombre, documento, correo, telefono, fecha_nacimiento) 
VALUES (LAST_INSERT_ID(), 'Andres Lopez', '91223344', 'andres@gmail.com', '3205556677', '1988-12-04');
INSERT INTO clientes (persona_id) VALUES (LAST_INSERT_ID());

UPDATE prestamos SET empleado_id = 1 WHERE id = 2;
SELECT id FROM empleados WHERE persona_id IN (SELECT id FROM personas WHERE correo LIKE '%carlos%');
UPDATE prestamos SET empleado_id = 1  WHERE id = 2;

INSERT INTO prestamos (cliente_id, empleado_id, monto, saldo_capital, interes, cuotas, fecha_solicitud, estado) 
VALUES (1, 1, 3000000.00, 3000000.00, 1.50, 12, CURDATE(), 'SOLICITADO');

INSERT INTO prestamos (cliente_id, empleado_id, monto, saldo_capital, interes, cuotas, fecha_solicitud, fecha_aprobacion, proxima_fecha_pago, estado) 
VALUES (2, 1, 5000000.00, 4800000.00, 2.00, 24, '2026-01-10', '2026-01-15', DATE_SUB(CURDATE(), INTERVAL 40 DAY), 'ACTIVO');

INSERT INTO cuotas_plan (prestamo_id, numero_cuota, fecha_vencimiento, capital_esperado, interes_esperado, capital_pendiente, interes_pendiente, estado) 
VALUES 
(2, 1, '2026-02-15', 200000.00, 50000.00, 0.00, 0.00, 'PAGADA'),
(2, 2, '2026-03-15', 200000.00, 50000.00, 0.00, 0.00, 'PAGADA'),
(2, 3, '2026-04-15', 200000.00, 50000.00, 200000.00, 50000.00, 'PENDIENTE');

INSERT INTO pagos (prestamo_id, metodo_pago, numero_referencia, fecha_reporte, fecha_aplicacion, monto, estado, empleado_revisor_id) 
VALUES 
(2, 'TRANSFERENCIA', 'REF-2026-001', '2026-02-14', '2026-02-14 10:30:00', 250000.00, 'APROBADO', 1),
(2, 'EFECTIVO', 'REF-2026-002', '2026-03-16', '2026-03-16 14:15:00', 250000.00, 'APROBADO', 1);

SELECT id, prestamo_id, metodo_pago, numero_referencia,
       monto, estado, fecha_reporte, fecha_aplicacion
FROM pagos
ORDER BY id DESC;