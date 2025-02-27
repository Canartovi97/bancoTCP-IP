package org.example.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaseDatos {
    private final String URL = "jdbc:postgresql://dpg-cuv9e6a3esus73bn0r8g-a.oregon-postgres.render.com:5432/distribuidos_3x52";
    private final String USUARIO = "distribuidos_3x52_user";
    private final String PASSWORD = "FRk8FC6h0IDPe9Gg2kPuUK4iFYWE6STP";

    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
