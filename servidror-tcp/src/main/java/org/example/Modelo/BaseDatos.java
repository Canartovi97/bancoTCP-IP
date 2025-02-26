package org.example.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseDatos {
    private static final String URL = "jdbc:postgresql://dpg-cuv9e6a3esus73bn0r8g-a.oregon-postgres.render.com:5432/distribuidos_3x52";
    private static final String USUARIO = "distribuidos_3x52_user";
    private static final String PASSWORD = "FRk8FC6h0IDPe9Gg2kPuUK4iFYWE6STP";

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    public String consultarSaldo(String input) {
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
