
CREATE DATABASE banco_db;


\c banco_db;


CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    numero_identificacion VARCHAR(20) UNIQUE NOT NULL,
    saldo DECIMAL(10,2) NOT NULL
);


INSERT INTO clientes (numero_cuenta, nombre, numero_identificacion, saldo) VALUES
('10001', 'Camilo Ramirez', '1032494811', 5000.50),
('10002', 'Maria González', '2007654321', 3200.75);
