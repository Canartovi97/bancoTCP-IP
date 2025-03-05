package org.example.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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





    /**
     * Realiza una consignación a la cuentaDestino incrementando su saldo.
     * Retorna true si se actualizó exitosamente, false si no existe la cuenta.
     */
    public boolean realizarConsignacion(String cuentaDestino, double monto) {
        String query = "UPDATE Cuentas SET saldo = saldo + ? WHERE numero_cuenta = ?";

        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, monto);
            stmt.setString(2, cuentaDestino);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("[Consultas] Consignación exitosa en cuenta: " + cuentaDestino);
                return true;
            } else {
                System.out.println("[Consultas] No se encontró la cuenta: " + cuentaDestino);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("[Consultas] Error en la consignación: " + e.getMessage());
            return false;
        }
    }
}
