package com.mycompany.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private ServerSocket serverSocket;
    private boolean running;
    private final int PORT = 12345;
    private Listener listener;
    private List<String> clientesConectados;
    private int contadorClientes;

    public Servidor(Listener listener) {
        this.listener = listener;
        this.clientesConectados = new ArrayList<>();
        this.contadorClientes = 1;
    }

    public void iniciarServidor() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                listener.mostrarMensaje("Servidor TCP en ejecución en puerto " + PORT);
                running = true;
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    String clienteNombre = "Cliente " + contadorClientes++;
                    clientesConectados.add(clienteNombre);
                    listener.actualizarListaClientes(clientesConectados);
                    new ClientHandler(clientSocket, listener, clienteNombre).start();
                }
            } catch (IOException e) {
                listener.mostrarMensaje("Error en el servidor: " + e.getMessage());
            }
        }).start();
    }

    public void detenerServidor() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            clientesConectados.clear();
            listener.actualizarListaClientes(clientesConectados);
            listener.mostrarMensaje("Servidor detenido.");
        } catch (IOException e) {
            listener.mostrarMensaje("Error al detener el servidor: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Listener listener;
    private String clienteNombre;

    public ClientHandler(Socket socket, Listener listener, String clienteNombre) {
        this.socket = socket;
        this.listener = listener;
        this.clienteNombre = clienteNombre;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                listener.mostrarMensaje(clienteNombre + ": " + mensaje);
                out.println("Mensaje recibido en el servidor");
            }
        } catch (IOException e) {
            listener.mostrarMensaje("Error en cliente " + clienteNombre + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
                listener.removerCliente(clienteNombre);
            } catch (IOException e) {
                listener.mostrarMensaje("Error cerrando cliente " + clienteNombre + ": " + e.getMessage());
            }
        }
    }
}
