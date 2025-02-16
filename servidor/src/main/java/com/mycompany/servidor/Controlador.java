package com.mycompany.servidor;

public class Controlador {
    private Vista vista;
    private Servidor servidor;

    public Controlador(Vista vista, Servidor servidor) {
        this.vista = vista;
        this.servidor = servidor;

        // Configurar los eventos de la vista
        this.vista.setControlador(this);
    }

    public void iniciarServidor() {
        vista.mostrarMensaje("Iniciando servidor...");
        servidor.iniciarServidor();
    }

    public void detenerServidor() {
        vista.mostrarMensaje("Deteniendo servidor...");
        servidor.detenerServidor();
    }

    public void recibirMensaje(String mensaje) {
        vista.mostrarMensaje("Cliente: " + mensaje);
    }
}