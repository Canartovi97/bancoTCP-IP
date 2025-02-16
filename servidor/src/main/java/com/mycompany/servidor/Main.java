/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.servidor;

import com.mycompany.servidor.*;

/**
 *
 * @author camilo
 */
public class Main {

    public static void main(String[] args) {
        
    	 java.awt.EventQueue.invokeLater(() -> {
    		 Vista vista = new Vista();
             Servidor servidor = new Servidor(vista);
             Controlador controlador = new Controlador(vista, servidor);
             vista.setControlador(controlador);
             vista.setVisible(true);
         });
    	
    	
    }
}
