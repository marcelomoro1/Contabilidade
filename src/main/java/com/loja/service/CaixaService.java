package com.loja.service;

import com.loja.model.LancamentoCaixa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CaixaService {
    LancamentoCaixa registrarLancamento(LancamentoCaixa lancamento);

    // Métodos para facilitar o registro de entrada/saída
    LancamentoCaixa registrarEntrada(BigDecimal valor, String descricao);
    LancamentoCaixa registrarSaida(BigDecimal valor, String descricao);

    BigDecimal calcularSaldoAtual();
    BigDecimal calcularSaldoPorData(LocalDate data);
    List<LancamentoCaixa> listarLancamentosPorPeriodo(LocalDate inicio, LocalDate fim);
}