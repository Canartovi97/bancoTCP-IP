package com.mycompany.servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseDatos {
    private static final String URL = "jdbc:postgresql://localhost:5432/banco_db";
    private static final String USUARIO = "postgres";
    private static final String PASSWORD = "123456";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    public static String consultarSaldo(String input) {
        String query = "SELECT nombre, saldo FROM clientes WHERE numero_cuenta = ? OR numero_identificacion = ?";
        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, input);
            stmt.setString(2, input);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                double saldo = rs.getDouble("saldo");
                return "Cliente: " + nombre + " - Saldo: $" + saldo;
            } else {
                return "No se encontró ninguna cuenta asociada.";
            }
        } catch (SQLException e) {
            return "Error en la consulta: " + e.getMessage();
        }
    }
}
