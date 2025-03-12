package org.example.Modelo;

import org.example.Controlador.Controlador;
import org.example.Vista.Listener;
import org.example.Vista.Vista;

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
    private Controlador controlador;





    private Listener listener;
    private List<String> clientesConectados;
    private List<String> autenticados = Collections.synchronizedList(new ArrayList<>());
    private List<PingHandler> pingHandlers;
    private Consultas consultas;
    private List<ClientHandler> clientHandlers;
    private int puertoBalanceador;
    private int puertoMonitoreo;

    public Servidor(Listener listener, Controlador controlador) {
        this.controlador = controlador;
        this.listener = listener;
        this.clientesConectados = new ArrayList<>();
        this.consultas = new Consultas();
        this.clientHandlers = new ArrayList<>();
        this.puertoBalanceador = -1;
        this.puertoMonitoreo = -1;

    }



    public void iniciarServidor() {
        registrarConBalanceador();

        if (puertoBalanceador == -1 || puertoMonitoreo == -1) {
            System.out.println("No se pudo obtener los puertos del balanceador. Cerrando servidor.");
            return;
        }

        if (controlador != null) {
            controlador.notificarPuerto(puertoBalanceador);
            System.out.println("Envio la notificacion " + puertoBalanceador);
        } else {
            System.out.println("No envio la notificacion " + puertoBalanceador);
        }

        // Iniciar el ServerSocket para recibir clientes en el puertoBalanceador
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(puertoBalanceador);
                running = true;
                System.out.println("Servidor Banco escuchando en el puerto " + puertoBalanceador);

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

        // Iniciar un ServerSocket en el puertoMonitoreo para responder al Monitoreo
        new Thread(() -> {
            try (ServerSocket serverSocketMonitoreo = new ServerSocket(puertoMonitoreo)) {
                System.out.println("[Servidor] Monitoreo escuchando en puerto " + puertoMonitoreo);

                while (running) {
                    Socket socket = serverSocketMonitoreo.accept();
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("OK_MONITORING"); // Respuesta al monitoreo
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("[Servidor] Error en monitoreo: " + e.getMessage());
            }
        }).start();
    }



    private void registrarConBalanceador() {
        try (Socket socket = new Socket(BALANCEADOR_IP, BALANCEADOR_PUERTO);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("[Servidor] Conectado al balanceador, esperando puertos...");
            String respuesta = entrada.readLine();

            if (respuesta != null && respuesta.matches("\\d+ \\d+")) {
                String[] partes = respuesta.split(" ");
                puertoBalanceador = Integer.parseInt(partes[0]);
                puertoMonitoreo = Integer.parseInt(partes[1]);

                // Aquí asignamos el puerto correcto para que el servidor escuche
                puertoAsignado = puertoBalanceador;

                System.out.println("[Servidor] Puertos asignados -> Balanceador: " + puertoBalanceador + ", Monitoreo: " + puertoMonitoreo);
            } else {
                System.out.println("[Servidor] Error: No se recibieron puertos válidos del balanceador.");
                puertoBalanceador = -1;
                puertoMonitoreo = -1;
                puertoAsignado = -1;
            }

        } catch (IOException e) {
            System.out.println("[Servidor] Error al conectar con el balanceador: " + e.getMessage());
            puertoBalanceador = -1;
            puertoMonitoreo = -1;
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
