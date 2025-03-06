package org.example.Modelo;

import org.example.Vista.Listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Servidor {
    private ServerSocket serverSocket;
    private ServerSocket pingServerSocket;
    private boolean running;
    private final int PORT = 12345;
    private final int PING_PORT = 12346;


    private Listener listener;
    private List<String> clientesConectados;
    private List<String> autenticados = Collections.synchronizedList(new ArrayList<>());
    private List<PingHandler> pingHandlers;
    private Consultas consultas;
    private List<ClientHandler> clientHandlers;


    public Servidor(Listener listener) {
        this.listener = listener;
        this.clientesConectados = new ArrayList<>();
        this.consultas = new Consultas();
        this.clientHandlers = new ArrayList<>();
        this.pingHandlers = new ArrayList<>();
    }

    /*public void iniciarServidor() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                System.out.println("Servidor TCP en ejecución en puerto " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    String clienteNombre = "Cliente-" + (clientHandlers.size() + 1);
                    clientesConectados.add(clienteNombre);

                    ClientHandler clientHandler = new ClientHandler(clientSocket, listener, clienteNombre, consultas, autenticados);
                    clientHandlers.add(clientHandler);
                    clientHandler.start();
                }
            } catch (IOException e) {
                System.err.println("Error en el servidor: " + e.getMessage());
            }
        }).start();
    }*/

    public void iniciarServidor() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                pingServerSocket = new ServerSocket(PING_PORT);  // **Iniciar socket de PING**
                running = true;

                if (listener != null) {
                    listener.mostrarMensaje("Servidor TCP en ejecución en puerto " + PORT);
                } else {
                    System.out.println("Servidor TCP en ejecución en puerto " + PORT);
                }

                // **Hilo para aceptar conexiones normales**
                new Thread(() -> {
                    while (running) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            String clienteNombre = "Cliente-" + (clientHandlers.size() + 1);
                            clientesConectados.add(clienteNombre);

                            ClientHandler clientHandler = new ClientHandler(clientSocket, listener, clienteNombre, consultas, autenticados);
                            clientHandlers.add(clientHandler);
                            clientHandler.start();
                        } catch (IOException ignored) {}
                    }
                }).start();

                // **Hilo para aceptar conexiones de PING**
                new Thread(() -> {
                    while (running) {
                        try {
                            Socket pingSocket = pingServerSocket.accept();
                            PingHandler pingHandler = new PingHandler(pingSocket);
                            pingHandlers.add(pingHandler);
                            pingHandler.start();
                        } catch (IOException ignored) {}
                    }
                }).start();
            } catch (IOException e) {
                System.err.println("Error en el servidor: " + e.getMessage());
            }
        }).start();
    }

    class PingHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public PingHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    String mensaje = in.readLine();
                    if (mensaje == null) break;
                    if (mensaje.equals("PING")) {
                        out.println("PONG");
                    }
                }
            } catch (IOException ignored) {}
        }
    }


    public void detenerServidor() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler handler : clientHandlers) {
                handler.detener();
            }
            clientHandlers.clear();
            clientesConectados.clear();
            System.out.println("Servidor detenido.");
        } catch (IOException e) {
            System.err.println("Error al detener el servidor: " + e.getMessage());
        }
    }


}
