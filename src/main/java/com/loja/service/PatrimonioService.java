package com.loja.service;

import com.loja.model.Patrimonio;
import java.util.List;
import java.util.Optional;

public interface PatrimonioService {
    Patrimonio salvar(Patrimonio patrimonio);
    void deletar(Long id);
    List<Patrimonio> listarTodos();
    Optional<Patrimonio> buscarPorId(Long id);
}