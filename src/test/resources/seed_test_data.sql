-- =========================================================================
-- Datos de referencia para la base de TESTING (peluqueria_test).
-- IDs alineados a mano con los enums EstadoTurno.java / EstadoFactura.java
-- para que el código de la app (que hardcodea esos IDs) funcione igual
-- que contra la base real.
-- =========================================================================

-- --- estado (turno) : debe coincidir 1:1 con claseslogicas/EstadoTurno.java
INSERT INTO estado (id_estado, nombre_estado) VALUES
  (1, 'Pendiente'),
  (2, 'Confirmado'),
  (3, 'Finalizado'),
  (4, 'Cancelado'),
  (5, 'Facturado (Pago Recibido)');
SELECT setval('estado_id_estado_seq', 5);

-- --- estado_factura : debe coincidir 1:1 con claseslogicas/EstadoFactura.java
INSERT INTO estado_factura (id_estado_factura, nombre) VALUES
  (1, 'Pendiente'),
  (2, 'Facturada'),
  (3, 'Anulada'),
  (4, 'Pagada');
SELECT setval('estado_factura_id_estado_factura_seq', 4);

-- --- roles
INSERT INTO roles (id_rol, nombre_rol, es_estilista) VALUES
  (1, 'Gerente', false),
  (2, 'Recepcionista', false),
  (3, 'Estilista', true);
SELECT setval('roles_id_rol_seq', 3);

-- --- tipo_documento
INSERT INTO tipo_documento (id_tipo_documento, tipo_documento) VALUES
  (1, 'DNI'),
  (2, 'CUIT'),
  (3, 'Pasaporte');
SELECT setval('tipo_documento_id_tipo_documento_seq', 3);

-- --- tipos_red_social
INSERT INTO tipos_red_social (id_tipo_red_social, tipo_red_social) VALUES
  (1, 'Instagram'),
  (2, 'Facebook');
SELECT setval('tipos_red_social_id_tipo_red_social_seq', 2);

-- --- metodo_pago (0% Efectivo/Débito, 10% recargo Crédito -> usado para validar cálculo de monto final)
INSERT INTO metodo_pago (id_metodo, nombre_metodo, porcentaje_modificador) VALUES
  (1, 'Efectivo', 0.00),
  (2, 'Tarjeta Débito', 0.00),
  (3, 'Tarjeta Crédito', 10.00);
SELECT setval('metodo_pago_id_metodo_seq', 3);

-- --- provincia / ciudad / barrio
-- CASO CRÍTICO (hallazgo #18): "San Martín" existe como ciudad en DOS provincias
-- distintas, con barrios propios y NO superpuestos. Cualquier consulta que
-- filtre barrios solo por nombre de ciudad (sin provincia) los mezclaría.
INSERT INTO provincia (id_provincia, nombre_provincia) VALUES
  (1, 'Buenos Aires'),
  (2, 'Mendoza'),
  (3, 'Córdoba');
SELECT setval('provincia_id_provincia_seq', 3);

INSERT INTO ciudad (id_ciudad, nombre_ciudad, id_provincia) VALUES
  (1, 'San Martín', 1),   -- San Martín, Buenos Aires
  (2, 'San Martín', 2),   -- San Martín, Mendoza (mismo nombre, otra provincia)
  (3, 'Laguna Larga', 3);
SELECT setval('ciudad_id_ciudad_seq', 3);

INSERT INTO barrio (id_barrio, nombre_barrio, id_ciudad) VALUES
  (1, 'Centro (San Martín BA)', 1),
  (2, 'Villa Ballester', 1),
  (3, 'Barrio Cívico (San Martín Mendoza)', 2),
  (4, 'Dorrego', 2),
  (5, 'Centro Laguna Larga', 3);
SELECT setval('barrio_id_barrio_seq', 5);

-- --- horario_atencion (CON TILDES a propósito: valida el fix del hallazgo #19)
INSERT INTO horario_atencion (id, "diaSemana", "horaApertura", "horaCierre") VALUES
  (1, 'Lunes', '09:00', '19:00'),
  (2, 'Martes', '09:00', '19:00'),
  (3, 'Miércoles', '09:00', '19:00'),
  (4, 'Jueves', '09:00', '19:00'),
  (5, 'Viernes', '09:00', '19:00'),
  (6, 'Sábado', '09:00', '14:00');
  -- Domingo: sin fila -> local cerrado ese día (caso "día sin horario" válido)
SELECT setval('horario_id_seq', 6);

-- --- servicios
INSERT INTO servicios (id_servicio, nombre_servicio, precio, descripcion, duracion_minutos) VALUES
  (1, 'Corte de pelo', 8000.00, 'Corte estándar', 30),
  (2, 'Coloración', 25000.00, 'Color completo', 90),
  (3, 'Peinado', 6000.00, 'Peinado para evento', 45);
SELECT setval('servicios_id_servicio_seq', 3);

-- --- personas + empleados + usuarios (Gerente, Recepcionista, Estilista)
-- Documentos propios para los empleados (no chocan con los de clientes de test)
INSERT INTO documento (id_documento, numero_documento, id_tipo_documento) VALUES
  (1, '30111222', 1),
  (2, '30222333', 1),
  (3, '30333444', 1);
SELECT setval('documento_id_documento_seq', 3);

INSERT INTO persona (id_persona, nombre, apellido, telefono, email, calle, numero, id_barrio, id_documento, activo) VALUES
  (1, 'Admin', 'Gerente', '3572400001', 'gerente@peluqpro.test', 'Falsa', '100', 1, 1, true),
  (2, 'Rosa', 'Recepcion', '3572400002', 'recepcion@peluqpro.test', 'Falsa', '101', 1, 2, true),
  (3, 'Estela', 'Estilista', '3572400003', 'estilista@peluqpro.test', 'Falsa', '102', 1, 3, true);
SELECT setval('persona_id_persona_seq', 3);

INSERT INTO empleado (id_empleado, id_persona, id_rol, fecha_ingreso) VALUES
  (1, 1, 1, '2024-01-01'),
  (2, 2, 2, '2024-01-01'),
  (3, 3, 3, '2024-01-01');
SELECT setval('empleado_id_empleado_seq', 3);

-- Segundo estilista, necesario para probar de verdad el filtro "por estilista"
-- (HU14): con uno solo no se puede distinguir "filtra bien" de "no filtra nada".
INSERT INTO documento (id_documento, numero_documento, id_tipo_documento) VALUES (4, '30444555', 1);
INSERT INTO persona (id_persona, nombre, apellido, telefono, email, calle, numero, id_barrio, id_documento, activo) VALUES
  (4, 'Marcos', 'Estilista2', '3572400004', 'estilista2@peluqpro.test', 'Falsa', '103', 1, 4, true);
INSERT INTO empleado (id_empleado, id_persona, id_rol, fecha_ingreso) VALUES (4, 4, 3, '2024-01-01');
SELECT setval('empleado_id_empleado_seq', 4);

-- Contraseñas reales (bcrypt), texto plano de referencia SOLO para los tests:
--   admin       / admin123        (Gerente)
--   recepcion   / recepcion123    (Recepcionista)
--   estilista1  / estilista123    (Estilista)
INSERT INTO usuarios (id, usuario, contrasena, id_rol_fk, id_empleado_fk) VALUES
  (1, 'admin', '$2a$12$gZ4UWeYgd3SZIIpjboGnLe34a8zLZtSFbkPCbpprta6uZKViIQFnK', 1, 1),
  (2, 'recepcion', '$2a$12$EtID0l8.W8FRv3alyPgpmO2bOpH2c2CQGXhVt3KOCNUQ7o.LQK9Ei', 2, 2),
  (3, 'estilista1', '$2a$12$a7IVv88a2imz5e.QwfjAFOI5Vj3vsJUZdk/9d9AXUF31HtBMipXoe', 3, 3);
SELECT setval('usuarios_id_seq', 3);

-- --- clientes de test fijos (documentos 40000001.. para no chocar con nada)
INSERT INTO documento (id_documento, numero_documento, id_tipo_documento) VALUES
  (10, '40000001', 1),  -- cliente activo normal
  (11, '40000002', 1);  -- cliente INACTIVO (para probar reactivación / exclusión de listados)
SELECT setval('documento_id_documento_seq', 11);

INSERT INTO persona (id_persona, nombre, apellido, telefono, email, calle, numero, id_barrio, id_documento, activo) VALUES
  (10, 'Juan', 'Perez', '3572500001', 'juan.perez@test.com', 'San Martín', '200', 1, 10, true),
  (11, 'Vieja', 'Inactiva', '3572500002', 'vieja.inactiva@test.com', 'San Martín', '201', 3, 11, false);
SELECT setval('persona_id_persona_seq', 11);

INSERT INTO cliente (id_cliente, id_persona, fecha_alta, numero_visitas, activo) VALUES
  (10, 10, CURRENT_DATE, 0, true),
  (11, 11, CURRENT_DATE, 0, false);
SELECT setval('cliente_id_cliente_seq', 11);

-- A partir de acá, cualquier persona/documento/cliente creado por los DAO
-- reales de la app (durante los tests) va a tomar ID >= 100, lo que permite
-- a TestDbSupport.limpiarDatosTransaccionales() distinguir "datos fijos del
-- seed" (id < 100) de "datos creados por un test" (id >= 100) y borrar solo
-- estos últimos entre corridas.
SELECT setval('persona_id_persona_seq', 99);
SELECT setval('documento_id_documento_seq', 99);
SELECT setval('cliente_id_cliente_seq', 99);
