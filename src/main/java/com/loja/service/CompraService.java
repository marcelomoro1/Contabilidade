package com.loja.service;

import com.loja.model.Compra;
import java.math.BigDecimal;
import java.util.List;

public interface CompraService {
    Compra salvar(Compra compra);
    void deletar(Long id);
    List<Compra> listarTodas();
    Compra buscarPorId(Long id);
    void registrarPagamento(Long compraId, BigDecimal valorPago); // <-- Adicione esta linha
}