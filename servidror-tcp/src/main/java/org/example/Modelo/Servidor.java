package org.example.Modelo;

import org.example.Vista.Listener;
import org.example.Vista.Vista;

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
    private Listener listener; // Puede ser null en modo headless
    private List<String> clientesConectados;
    private int contadorClientes;
    private Consultas consultas;

    public Servidor(Listener listener) {
        this.listener = listener; // Puede ser null en modo headless
        this.clientesConectados = new ArrayList<>();
        this.contadorClientes = 1;
        this.consultas = new Consultas();
    }

    public void iniciarServidor() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                if (listener != null) {
                    listener.mostrarMensaje("Servidor TCP en ejecución en puerto " + PORT);
                } else {
                    System.out.println("Servidor TCP en ejecución en puerto " + PORT);
                }
                running = true;
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    String clienteNombre = "Cliente " + contadorClientes++;
                    clientesConectados.add(clienteNombre);

                    if (listener != null) {
                        listener.actualizarListaClientes(clientesConectados);
                    } else {
                        System.out.println("Cliente conectado: " + clienteNombre);
                    }

                    new ClientHandler(clientSocket, listener, clienteNombre, consultas).start();
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
}

class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Listener listener;
    private String clienteNombre;
    private Consultas consultas;

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


            boolean autenticado = manejarAutenticacion();
            if (!autenticado) {
                socket.close();
                return;
            }


            manejarComandos();
        } catch (IOException e) {
            System.out.println("Error en cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error cerrando cliente: " + e.getMessage());
            }
        }
    }


    private boolean manejarAutenticacion() throws IOException {
        out.println("Ingrese usuario y contraseña separados por espacio:");
        String mensaje = in.readLine();
        if (mensaje != null) {
            System.out.println("Servidor recibió (login): [" + mensaje + "]");
            String[] partes = mensaje.trim().split("\\s+");
            if (partes.length == 2) {
                String username = partes[0];
                String password = partes[1];
                boolean autenticado = consultas.verificarCredenciales(username, password);
                if (autenticado) {
                    System.out.println("Servidor autenticó correctamente a: " + username);
                    out.println("LOGIN_EXITO " + username);

                    if (listener != null) {
                        listener.mostrarMensaje("Usuario autenticado: " + username);
                    }

                    return true;
                } else {
                    System.out.println("Servidor: Credenciales incorrectas");
                    out.println("LOGIN_FALLIDO");

                    if (listener != null) {
                        listener.mostrarMensaje("Credenciales incorrectas para: " + username);
                    }

                    return false;
                }
            }
        }
        System.out.println("Servidor: Formato de mensaje inválido");
        out.println("ERROR: Formato inválido");
        return false;
    }


    private void manejarComandos() throws IOException {
        String mensaje;
        while ((mensaje = in.readLine()) != null) {
            System.out.println("Servidor recibió: [" + mensaje + "]");
            if (mensaje.startsWith("CONSULTAR_SALDO")) {
                manejarConsultaSaldo(mensaje);
            } else if (mensaje.startsWith("CONSIGNAR")) {
                manejarConsignacion(mensaje);
            } else {
                System.out.println("Servidor: Comando no reconocido [" + mensaje + "]");
                out.println("ERROR: Comando no reconocido.");

                if (listener != null) {
                    listener.mostrarMensaje("Comando no reconocido: " + mensaje);
                }
            }
        }
    }

    private void manejarConsultaSaldo(String mensaje) {

        String numeroCuenta = mensaje.replaceFirst("CONSULTAR_SALDO\\s+", "").trim();

        if (!numeroCuenta.isEmpty()) {
            System.out.println("Servidor: Ejecutando consulta para la cuenta " + numeroCuenta);
            String respuesta = consultas.consultarSaldo(numeroCuenta);
            System.out.println("Servidor responde: [" + respuesta + "]");


            out.println("SALDO_OK " + respuesta);
        } else {
            System.out.println("Servidor: Número de cuenta no válido");
            out.println("ERROR: Número de cuenta inválido.");
        }
    }



    private void manejarConsignacion(String mensaje) {
        String[] partes = mensaje.split("\\s+");

        if (partes.length != 3) {
            out.println("ERROR: Formato de consignación inválido.");
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

        System.out.println("Servidor: Procesando consignación a " + cuentaDestino + " por $" + monto);

        boolean exito = consultas.realizarConsignacion(cuentaDestino, monto);

        if (exito) {
            out.println("CONSIGNACION_EXITOSA " + cuentaDestino + " $" + monto);
        } else {
            out.println("ERROR: No se pudo realizar la consignación.");
        }
    }







}
