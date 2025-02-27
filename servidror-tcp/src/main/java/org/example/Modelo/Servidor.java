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
    private Listener listener;
    private List<String> clientesConectados;
    private int contadorClientes;
    private Consultas consultas;

    public Servidor(Vista listener) {
        this.listener = listener;
        this.clientesConectados = new ArrayList<>();
        this.contadorClientes = 1;
        this.consultas = new Consultas();
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
                    new ClientHandler(clientSocket, listener, clienteNombre, consultas).start();
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
                    return true;
                } else {
                    System.out.println("Servidor: Credenciales incorrectas");
                    out.println("LOGIN_FALLIDO");
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








}
