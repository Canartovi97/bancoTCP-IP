package org.example.Vista;

import org.example.Controlador.Controlador;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Vista extends JFrame implements Listener {
    private Controlador controlador;
    private DefaultListModel<String> listaClientesModel;
    private int contadorClientes = 1;

    public Vista() {
        initComponents();
        listaClientesModel = new DefaultListModel<>();
        jListClientes.setModel(listaClientesModel);
    }

    private void initComponents() {
        setTitle("Servidor Banco TCP/IP");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        jButton2 = new JButton("Iniciar");
        jButton3 = new JButton("Apagar");
        jLabel1 = new JLabel("Servidor Banco TCP/IP", SwingConstants.CENTER);
        jLabel2 = new JLabel("Dispositivos Conectados");
        jLabel3 = new JLabel("Mensajes");
        
        jTextArea2 = new JTextArea(10, 30);
        jTextArea2.setEditable(false);
        jScrollPane2 = new JScrollPane(jTextArea2);
        
        jListClientes = new JList<>();
        jScrollPane1 = new JScrollPane(jListClientes);

        setLayout(null);
        
        jLabel1.setBounds(150, 10, 300, 30);
        jButton2.setBounds(50, 50, 200, 30);
        jButton3.setBounds(300, 50, 200, 30);
        jLabel2.setBounds(50, 100, 200, 20);
        jLabel3.setBounds(300, 100, 200, 20);
        jScrollPane1.setBounds(50, 130, 200, 200);
        jScrollPane2.setBounds(300, 130, 250, 200);

        add(jLabel1);
        add(jButton2);
        add(jButton3);
        add(jLabel2);
        add(jLabel3);
        add(jScrollPane1);
        add(jScrollPane2);
        
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (controlador != null) controlador.iniciarServidor();
            }
        });

        jButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (controlador != null) controlador.detenerServidor();
            }
        });
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        jTextArea2.append(mensaje + "\n");
    }

    public void actualizarListaClientes(List<String> clientes) {
        listaClientesModel.clear();
        for (String cliente : clientes) {
            listaClientesModel.addElement(cliente);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Vista().setVisible(true);
            }
        });
    }

    private JButton jButton2;
    private JButton jButton3;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JTextArea jTextArea2;
    private JList<String> jListClientes;

	@Override
	public void removerCliente(String cliente) {
		// TODO Auto-generated method stub
		
	}
}
