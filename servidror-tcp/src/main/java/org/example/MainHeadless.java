package org.example;

import org.example.Controlador.Controlador;
import org.example.Modelo.Servidor;

public class MainHeadless {
    public static void main(String[] args) {
        Servidor servidor = new Servidor(null);
        Controlador controlador = new Controlador(null, servidor);
        controlador.iniciarServidorHeadless();
    }
}