package org.example;


import org.example.Controlador.Controlador;
import org.example.Modelo.Servidor;
import org.example.Vista.Vista;

public class Main {
    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(() -> {
            Vista vista = new Vista();
            Controlador controlador = new Controlador(vista, null);
            Servidor servidor = new Servidor(vista, controlador);

            controlador.setServidor(servidor);

            vista.setControlador(controlador);
            vista.setVisible(true);

            if (controlador != null) {
                try {
                    controlador.iniciarServidor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });




    }
}