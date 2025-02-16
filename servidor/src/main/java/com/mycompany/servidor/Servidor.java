package com.mycompany.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    private ServerSocket serverSocket;
    private boolean running;
    private final int PORT = 12345;
    private Listener listener; 

    public Servidor(Listener listener) {
        this.listener = listener;
    }

    public void iniciarServidor() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                listener.mostrarMensaje("Servidor TCP en ejecución en puerto " + PORT);
                running = true;
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket, listener).start();
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

    public ClientHandler(Socket socket, Listener listener) {
        this.socket = socket;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                listener.mostrarMensaje("Cliente: " + mensaje);
                out.println("Mensaje recibido en el servidor");
            }
        } catch (IOException e) {
            listener.mostrarMensaje("Error en cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                listener.mostrarMensaje("Error cerrando cliente: " + e.getMessage());
            }
        }
    }
}
