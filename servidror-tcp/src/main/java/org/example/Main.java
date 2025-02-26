package org.example;


import org.example.Controlador.Controlador;
import org.example.Modelo.Servidor;
import org.example.Vista.Vista;

public class Main {
    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(() -> {
            Vista vista = new Vista();
            Servidor servidor = new Servidor(vista);
            org.example.Controlador.Controlador controlador = new Controlador(vista, servidor);
            vista.setControlador(controlador);
            vista.setVisible(true);
        });




    }
}