package com.mycompany.servidor;

import com.mycompany.servidor.Controlador;
import com.mycompany.servidor.Listener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Vista extends JFrame implements Listener {
    private JButton bIniciar, bDetener;
    private JTextArea mensajesTxt;
    private Controlador controlador;

    public Vista() {
        initComponents();
    }

    private void initComponents() {
        this.setTitle("Servidor TCP");
        this.setSize(500, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(null);

        JLabel label = new JLabel("Servidor TCP");
        label.setBounds(200, 10, 100, 20);
        this.add(label);

        bIniciar = new JButton("Iniciar Servidor");
        bIniciar.setBounds(50, 50, 150, 30);
        this.add(bIniciar);

        bDetener = new JButton("Detener Servidor");
        bDetener.setBounds(250, 50, 150, 30);
        this.add(bDetener);

        mensajesTxt = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(mensajesTxt);
        scrollPane.setBounds(50, 100, 350, 150);
        this.add(scrollPane);

        bIniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controlador != null) controlador.iniciarServidor();
            }
        });

        bDetener.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controlador != null) controlador.detenerServidor();
            }
        });
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        mensajesTxt.append(mensaje + "\n");
    }
}
