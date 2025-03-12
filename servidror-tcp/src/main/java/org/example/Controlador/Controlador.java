package org.example.Controlador;

import org.example.Modelo.Servidor;
import org.example.Vista.Vista;

import java.util.ArrayList;
import java.util.List;

public class Controlador {
    private Vista vista;
    private Servidor servidor;
    private List<String> clientesConectados;
    private int contadorClientes;

    public Controlador(Vista vista, Servidor servidor) {
        this.vista = vista;
        this.servidor = servidor;
        this.clientesConectados = new ArrayList<>();
        this.contadorClientes = 1;

        if (this.vista != null) {
            this.vista.setControlador(this);
        }
    }

    public void iniciarServidor() {
        if (vista != null) {
            vista.mostrarMensaje("Iniciando servidor...");
        } else {
            System.out.println("Iniciando servidor asdasd...");
        }
        servidor.iniciarServidor();
    }

    public void detenerServidor() {
        if (vista != null) {
            vista.mostrarMensaje("Deteniendo servidor...");
            clientesConectados.clear();
            vista.actualizarListaClientes(clientesConectados);
        } else {
            System.out.println("Deteniendo servidor...");
            clientesConectados.clear();
        }
        servidor.detenerServidor();
    }


    public void notificarPuerto(int puerto) {
        if (vista != null) {
            vista.mostrarMensaje("Servidor iniciado en el puerto " + puerto);
        } else {
            System.out.println("Servidor iniciado en el puerto " + puerto);
        }
    }

    public void setServidor(Servidor servidor) {
        this.servidor = servidor;
    }



}