package com.loja.service.impl;

import com.loja.model.LancamentoCaixa;
import com.loja.model.enums.TipoLancamentoCaixa;
import com.loja.repository.LancamentoCaixaRepository;
import com.loja.service.CaixaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CaixaServiceImpl implements CaixaService {

    @Autowired
    private LancamentoCaixaRepository lancamentoCaixaRepository;

    @Override
    @Transactional
    public LancamentoCaixa registrarLancamento(LancamentoCaixa lancamento) {
        if (lancamento.getDataHora() == null) {
            lancamento.setDataHora(LocalDateTime.now());
        }
        return lancamentoCaixaRepository.save(lancamento);
    }

    @Override
    @Transactional
    public LancamentoCaixa registrarEntrada(BigDecimal valor, String descricao) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da entrada deve ser maior que zero.");
        }
        return registrarLancamento(new LancamentoCaixa(TipoLancamentoCaixa.ENTRADA, valor, descricao));
    }

    @Override
    @Transactional
    public LancamentoCaixa registrarSaida(BigDecimal valor, String descricao) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da saída deve ser maior que zero.");
        }
        return registrarLancamento(new LancamentoCaixa(TipoLancamentoCaixa.SAIDA, valor, descricao));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoAtual() {
        return lancamentoCaixaRepository.calcularSaldoTotal();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoPorData(LocalDate data) {
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX); // Até o final do dia
        return lancamentoCaixaRepository.calcularSaldoAteData(fimDoDia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LancamentoCaixa> listarLancamentosPorPeriodo(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDoDia = inicio.atStartOfDay();
        LocalDateTime fimDoDia = fim.atTime(LocalTime.MAX);
        return lancamentoCaixaRepository.findByDataHoraBetweenOrderByDataHoraAsc(inicioDoDia, fimDoDia);
    }
}