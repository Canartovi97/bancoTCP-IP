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
    private boolean running;
    private final int PORT = 12345;

    private Listener listener;
    private List<String> clientesConectados;



    private static final List<String> autenticados = Collections.synchronizedList(new ArrayList<>());


    private Consultas consultas;
    private List<ClientHandler> clientHandlers;

    public Servidor(Listener listener) {
        this.listener = listener;
        this.clientesConectados = new ArrayList<>();
        this.consultas = new Consultas();
        this.clientHandlers = new ArrayList<>();
    }

    public void iniciarServidor() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;

                if (listener != null) {
                    listener.mostrarMensaje("Servidor TCP en ejecución en puerto " + PORT);
                } else {
                    System.out.println("Servidor TCP en ejecución en puerto " + PORT);
                }

                while (running) {
                    Socket clientSocket = serverSocket.accept();

                    String clienteNombre = "Cliente-" + (clientHandlers.size() + 1);
                    clientesConectados.add(clienteNombre);
                    if (listener != null) {
                        listener.actualizarListaClientes(clientesConectados);
                    } else {
                        System.out.println("Nuevo cliente conectado: " + clienteNombre);
                    }

                    ClientHandler clientHandler = new ClientHandler(clientSocket, listener, clienteNombre, consultas);
                    clientHandlers.add(clientHandler);
                    clientHandler.start();
                }
            } catch (IOException e) {
                if (listener != null) {
                    listener.mostrarMensaje("Error en el servidor: " + e.getMessage());
                } else {
                    System.err.println("Error en el servidor: " + e.getMessage());
                }
            }
        }).start();
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

            if (listener != null) {
                listener.actualizarListaClientes(clientesConectados);
                listener.mostrarMensaje("Servidor detenido.");
            } else {
                System.out.println("Servidor detenido.");
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.mostrarMensaje("Error al detener el servidor: " + e.getMessage());
            } else {
                System.err.println("Error al detener el servidor: " + e.getMessage());
            }
        }
    }

    /**
     * ClientHandler interno que maneja login + comandos.
     * Usa la estructura 'autenticados' para verificar si un user ya está logueado.
     */
    class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Listener listener;
        private String clienteNombre;
        private Consultas consultas;
        private volatile boolean running = true;

        public ClientHandler(Socket socket, Listener listener, String clienteNombre, Consultas consultas) {
            this.socket = socket;
            this.listener = listener;
            this.clienteNombre = clienteNombre;
            this.consultas = consultas;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                manejarAutenticacion();
                manejarComandos();

            } catch (IOException e) {
                System.out.println("[ClientHandler] Error: " + e.getMessage());
            } finally {
                detener();
            }
        }

        public void detener() {
            running = false;
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {}
        }

        /**
         * Maneja la lógica de login con un approach:
         * 1) Recibe "LOGIN user pass"
         * 2) Si user ya está en 'autenticados', responde "USUARIO_YA_AUTENTICADO".
         * 3) Sino, verifica en BD, si OK -> "LOGIN_EXITO", y añade a 'autenticados'.
         * 4) Si falla -> "LOGIN_FALLIDO"
         */


        private void manejarAutenticacion() throws IOException {
            String mensaje = in.readLine();
            if (mensaje == null) {
                out.println("ERROR: No llega mensaje de login.");
                throw new IOException("No llega mensaje de login.");
            }
            System.out.println("el servidor recibe  (login): [" + mensaje + "]");

            String[] partes = mensaje.trim().split("\\s+");

            if (partes.length == 3 && partes[0].equalsIgnoreCase("LOGIN")) {
                String user = partes[1];
                String pass = partes[2];


                if (autenticados.contains(user)) {
                    System.out.println("Error el  usuario ya esta autenticado: " + user);
                    out.println("USUARIO_YA_AUTENTICADO");
                    return;
                }

                // 2) Verificar en BD
                boolean ok = consultas.verificarCredenciales(user, pass);
                if (ok) {
                    autenticados.add(user);
                    System.out.println("Usuario autenticado: " + user);
                    out.println("LOGIN_EXITO");


                } else {
                    System.out.println("[ClientHandler] Credenciales incorrectas: " + user);
                    out.println("LOGIN_FALLIDO");

                }
            } else {
                out.println("ERROR: Formato de login inválido. Use: LOGIN <user> <pass>");
                throw new IOException("Formato de login inválido");
            }
        }


        private void manejarComandos() throws IOException {
            String linea;
            while (running && (linea = in.readLine()) != null) {
                System.out.println("[ClientHandler] Recibido comando: [" + linea + "]");

                if (linea.startsWith("CONSULTAR_SALDO")) {
                    manejarConsultaSaldo(linea);

                } else if (linea.startsWith("CONSIGNAR")) {
                    manejarConsignacion(linea);

                } else {
                    System.out.println("[ClientHandler] Comando no reconocido: [" + linea + "]");
                    out.println("ERROR: Comando no reconocido.");
                }
            }
        }



        private void manejarConsultaSaldo(String mensaje) {
            String[] partes = mensaje.trim().split("\\s+");

            if (partes.length != 4) {
                out.println("ERROR: Formato inválido. Use: CONSULTAR_SALDO <username> <numero> <CUENTA|CEDULA>");
                return;
            }

            String username = partes[1]; // Usuario autenticado
            String numero = partes[2];   // Número ingresado (cuenta o cédula)
            String tipo = partes[3];     // Tipo de consulta (CUENTA o CEDULA)

            if (!autenticados.contains(username)) {
                out.println("ERROR: Usuario no autenticado.");
                return;
            }

            String respuesta = consultas.consultarSaldo(username, tipo, numero);
            out.println(respuesta);
        }

        private void manejarConsignacion(String linea) {
            String[] partes = linea.trim().split("\\s+");
            if (partes.length != 3) {
                out.println("ERROR: Formato consignación inválido. Use: CONSIGNAR <cuenta> <monto>");
                return;
            }
            String cuentaDestino = partes[1];
            double monto;
            try {
                monto = Double.parseDouble(partes[2]);
            } catch (NumberFormatException e) {
                out.println("ERROR: Monto inválido.");
                return;
            }
            boolean exito = consultas.realizarConsignacion(cuentaDestino, monto);
            if (exito) {
                out.println("CONSIGNACION_EXITOSA " + cuentaDestino + " $" + monto);
            } else {
                out.println("ERROR: No se pudo realizar la consignación.");
            }
        }
    }
}
