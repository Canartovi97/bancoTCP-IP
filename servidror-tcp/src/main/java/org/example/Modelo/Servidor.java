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

    private final int PORT = 12345;
    private final int PING_PORT = 12346;

    private boolean running;
    private int puertoAsignado;
    private static final String BALANCEADOR_IP = "127.0.0.1";
    private static final int BALANCEADOR_PUERTO = 5000;





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
    }



    public void iniciarServidor() {
        registrarConBalanceador();

        if (puertoAsignado == -1) {
            System.out.println("No se pudo obtener un puerto del balanceador. Cerrando servidor.");
            return;
        }

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(puertoAsignado);
                running = true;
                System.out.println("Servidor Banco escuchando en el puerto " + puertoAsignado);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    String clienteNombre = "Cliente-" + (clientHandlers.size() + 1);
                    clientesConectados.add(clienteNombre);

                    ClientHandler clientHandler = new ClientHandler(this, clientSocket, listener, clienteNombre, consultas, autenticados);
                    clientHandlers.add(clientHandler);
                    clientHandler.start();
                }
            } catch (IOException e) {
                System.err.println("Error en el servidor: " + e.getMessage());
            }
        }).start();
    }

    private void registrarConBalanceador() {
        try (Socket socket = new Socket(BALANCEADOR_IP, BALANCEADOR_PUERTO);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("[Servidor] Conectado al balanceador, esperando IP...");
            String respuesta = entrada.readLine();

            if (respuesta != null && respuesta.matches("\\d+")) {
                puertoAsignado = Integer.parseInt(respuesta);
                System.out.println("[Servidor] IP asignada por el balanceador: " + puertoAsignado);
            } else {
                System.out.println("[Servidor] Error: No se recibió una IP válida del balanceador.");
                puertoAsignado = -1;
            }

        } catch (IOException e) {
            System.out.println("[Servidor] Error al conectar con el balanceador: " + e.getMessage());
            puertoAsignado = -1;
        }
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





    public synchronized void actualizarListaClientes() {
        List<String> usuariosAutenticados = new ArrayList<>(autenticados);
        if (listener != null) {
            listener.actualizarListaClientes(usuariosAutenticados);
        }
    }


}
