package org.example;

import org.example.Controlador.Controlador;
import org.example.Modelo.Servidor;

public class MainHeadless {
    public static void main(String[] args) {
        // Iniciar el servidor en modo headless
        Servidor servidor = new Servidor(null); // Sin vista
        Controlador controlador = new Controlador(null, servidor);
        controlador.iniciarServidorHeadless();
    }
}