package com.loja.service;

import com.loja.model.Cliente;
import java.util.List;

public interface ClienteService {
    List<Cliente> listarTodos();
    Cliente buscarPorId(Long id);
    Cliente salvar(Cliente cliente);
    void deletar(Long id);
    boolean existePorCpf(String cpf);
    long countClientes();
} 