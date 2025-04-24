package com.loja.service;

import com.loja.model.Venda;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VendaService {
    Venda salvar(Venda venda);
    List<Venda> listarTodas();
    List<Venda> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim);
    Venda buscarPorId(Long id);
    void deletar(Long id);
    Long countVendasByData(LocalDate data);
    Double calcularFaturamentoPorData(LocalDate data);
} 