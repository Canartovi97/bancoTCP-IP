package org.example.Modelo;

import org.example.Vista.Listener;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Listener listener;
    private String clienteNombre;
    private Consultas consultas;
    private List<String> autenticados;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, Listener listener, String clienteNombre, Consultas consultas, List<String> autenticados) {
        this.socket = socket;
        this.listener = listener;
        this.clienteNombre = clienteNombre;
        this.consultas = consultas;
        this.autenticados = autenticados;
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

    private void manejarAutenticacion() throws IOException {
        String mensaje = in.readLine();
        if (mensaje == null) {
            out.println("ERROR: No llega mensaje de login.");
            throw new IOException("No llega mensaje de login.");
        }

        System.out.println("El servidor recibe (login): [" + mensaje + "]");

        String[] partes = mensaje.trim().split("\\s+");
        if (partes.length == 3 && partes[0].equalsIgnoreCase("LOGIN")) {
            String user = partes[1];
            String pass = partes[2];

            if (autenticados.contains(user)) {
                System.out.println("Error: Usuario ya autenticado: " + user);
                out.println("USUARIO_YA_AUTENTICADO");
                return;
            }

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
            } else if (linea.equals("PING")) {
                out.println("PONG");
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

        String username = partes[1];
        String numero = partes[2];
        String tipo = partes[3];

        if (!autenticados.contains(username)) {
            out.println("ERROR: Usuario no autenticado.");
            return;
        }

        String respuesta = consultas.consultarSaldo(username, tipo, numero);
        System.out.println("[Servidor] Enviando respuesta al cliente: " + respuesta);
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
