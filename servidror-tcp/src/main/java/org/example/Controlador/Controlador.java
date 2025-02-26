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
        this.vista.setControlador(this);
    }

    public void iniciarServidor() {
        vista.mostrarMensaje("Iniciando servidor...");
        servidor.iniciarServidor();
    }

    public void detenerServidor() {
        vista.mostrarMensaje("Deteniendo servidor...");
        servidor.detenerServidor();
        clientesConectados.clear();
        vista.actualizarListaClientes(clientesConectados);
    }

    public void agregarCliente() {
        String cliente = "Cliente " + contadorClientes++;
        clientesConectados.add(cliente);
        vista.actualizarListaClientes(clientesConectados);
    }

    public void eliminarCliente(String cliente) {
        clientesConectados.remove(cliente);
        vista.actualizarListaClientes(clientesConectados);
    }
}
