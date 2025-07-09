package com.loja.service;

import com.loja.model.Fornecedor;
import java.util.List;

public interface FornecedorService {
    List<Fornecedor> listarTodos();
    Fornecedor buscarPorId(Long id);
    Fornecedor salvar(Fornecedor fornecedor);
    void deletar(Long id);
} 