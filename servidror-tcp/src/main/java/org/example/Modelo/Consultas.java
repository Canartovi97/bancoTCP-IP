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

            System.out.println("Ejecutando consulta SQL: " + query);
            System.out.println("Parámetros: Usuario=" + username + ", Password=" + password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Usuario autenticado correctamente: " + username);
                return true;
            } else {
                System.out.println("Error: Credenciales incorrectas para usuario: " + username);
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error en la consulta de login: " + e.getMessage());
            return false;
        }
    }

    public String consultarSaldo(String numeroCuenta) {
        String query = "SELECT numero_cuenta, saldo FROM Cuentas WHERE numero_cuenta = ?";

        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, numeroCuenta);
            System.out.println("Ejecutando consulta: " + query);
            System.out.println("Parametros -> Cuenta: " + numeroCuenta);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String cuenta = rs.getString("numero_cuenta");
                double saldo = rs.getDouble("saldo");
                return "Cuenta: " + cuenta + " - Saldo: $" + saldo;
            } else {
                return "Error: No se encontró ninguna cuenta con ese número.";
            }
        } catch (SQLException e) {
            return "Error en la consulta de saldo: " + e.getMessage();
        }
    }



    public boolean realizarConsignacion(String cuentaDestino, double monto) {
        String query = "UPDATE Cuentas SET saldo = saldo + ? WHERE numero_cuenta = ?";

        try (Connection conn = baseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, monto);
            stmt.setString(2, cuentaDestino);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Servidor: Consignación exitosa en cuenta " + cuentaDestino);
                return true;
            } else {
                System.out.println("Servidor: No se encontró la cuenta " + cuentaDestino);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error en la consignación: " + e.getMessage());
            return false;
        }
    }



}
