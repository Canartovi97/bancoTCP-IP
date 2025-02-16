package com.mycompany.servidor;

import java.util.List;

public interface Listener {
    void mostrarMensaje(String mensaje);
    void actualizarListaClientes(List<String> clientes);
    void removerCliente(String cliente);
}
