package org.example.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Consultas {
    private BaseDatos baseDatos;

    public Consultas() {
        this.baseDatos = new BaseDatos();
    }


    public boolean verificarCredenciales(String username, String password) {
        String query = "SELECT * FROM Usuarios WHERE username = ? AND password = ?";

        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            System.out.println("[Consultas] Ejecutando: " + query);
            System.out.println("[Consultas] Parámetros -> Usuario=" + username + ", Password=" + password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("[Consultas] Usuario autenticado: " + username);
                return true;
            } else {
                System.out.println("[Consultas] Credenciales incorrectas para usuario: " + username);
                return false;
            }

        } catch (SQLException e) {
            System.out.println("[Consultas] Error en la consulta de login: " + e.getMessage());
            return false;
        }
    }




    public String consultarSaldo(String username, String tipo, String valor) {
        if (tipo.equalsIgnoreCase("CUENTA")) {
            return consultarSaldoPorCuenta(username, valor);
        } else if (tipo.equalsIgnoreCase("CEDULA")) {
            return consultarSaldoPorCedula(username, valor);
        } else {
            return "ERROR: Tipo de consulta inválido.";
        }
    }

    private String consultarSaldoPorCuenta(String username, String numeroCuenta) {
        String query = "SELECT c.saldo FROM Cuentas c " +
                "INNER JOIN Usuarios u ON c.id_persona = u.id_persona " +
                "WHERE u.username = ? AND c.numero_cuenta = ?";

        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, numeroCuenta);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double saldo = rs.getDouble("saldo");
                return "Saldo disponible: $" + saldo;
            } else {
                return "ERROR: No se encontró la cuenta o no pertenece al usuario.";
            }
        } catch (SQLException e) {
            return "ERROR en la consulta de saldo: " + e.getMessage();
        }
    }

    private String consultarSaldoPorCedula(String username, String documento) {
        String query = "SELECT c.numero_cuenta, c.saldo FROM Cuentas c " +
                "INNER JOIN Personas p ON c.id_persona = p.id_persona " +
                "INNER JOIN Usuarios u ON p.id_persona = u.id_persona " +
                "WHERE u.username = ? AND p.documento = ?";

        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, documento);

            ResultSet rs = stmt.executeQuery();
            StringBuilder resultado = new StringBuilder("Cuentas y saldos:");

            while (rs.next()) {
                String cuenta = rs.getString("numero_cuenta");
                double saldo = rs.getDouble("saldo");


                resultado.append(" \\n Cuenta: ").append(cuenta)
                        .append(" | Saldo: $").append(saldo);
            }

            String respuestaFinal = resultado.toString();
            System.out.println("[Consultas] Respuesta enviada al cliente: " + respuestaFinal);
            return respuestaFinal;

        } catch (SQLException e) {
            return "ERROR en la consulta de saldo por cédula: " + e.getMessage();
        }
    }


    public List<String> obtenerCuentasPorUsuario(String username) {
        String query = "SELECT c.numero_cuenta FROM Cuentas c " +
                "INNER JOIN Usuarios u ON c.id_persona = u.id_persona " +
                "WHERE u.username = ?";

        List<String> cuentas = new ArrayList<>();
        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cuentas.add(rs.getString("numero_cuenta"));
            }
        } catch (SQLException e) {
            System.out.println("[Consultas] Error al obtener cuentas: " + e.getMessage());
        }
        return cuentas;
    }





    public boolean realizarConsignacion(String cuentaOrigen, String cuentaDestino, double monto) {
        String queryObtenerIdCuenta = "SELECT id_cuenta FROM Cuentas WHERE numero_cuenta = ?";
        String queryInsertarMovimiento = "INSERT INTO movimientos (fecha_hora, monto, descripcion, id_cuenta_origen, id_cuenta_destino) VALUES (NOW(), ?, 'Consignación', ?, ?)";

        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmtObtenerIdOrigen = conn.prepareStatement(queryObtenerIdCuenta);
             PreparedStatement stmtObtenerIdDestino = conn.prepareStatement(queryObtenerIdCuenta);
             PreparedStatement stmtMovimiento = conn.prepareStatement(queryInsertarMovimiento)) {

            conn.setAutoCommit(false);

            // Obtener ID de la cuenta origen
            stmtObtenerIdOrigen.setString(1, cuentaOrigen);
            ResultSet rsOrigen = stmtObtenerIdOrigen.executeQuery();
            Integer idCuentaOrigen = rsOrigen.next() ? rsOrigen.getInt("id_cuenta") : null;

            // Obtener ID de la cuenta destino
            stmtObtenerIdDestino.setString(1, cuentaDestino);
            ResultSet rsDestino = stmtObtenerIdDestino.executeQuery();
            Integer idCuentaDestino = rsDestino.next() ? rsDestino.getInt("id_cuenta") : null;

            // Verificar si ambas cuentas existen
            if (idCuentaOrigen == null || idCuentaDestino == null) {
                System.out.println("[Consultas] ERROR: Una de las cuentas no existe.");
                return false;
            }

            // Registrar movimiento
            stmtMovimiento.setDouble(1, monto);
            stmtMovimiento.setInt(2, idCuentaOrigen);
            stmtMovimiento.setInt(3, idCuentaDestino);
            stmtMovimiento.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("[Consultas] ERROR en la consignación: " + e.getMessage());
            return false;
        }
    }





}
