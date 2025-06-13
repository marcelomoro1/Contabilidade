package com.loja.repository;

import com.loja.model.LancamentoCaixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LancamentoCaixaRepository extends JpaRepository<LancamentoCaixa, Long> {

    List<LancamentoCaixa> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(CASE WHEN l.tipo = 'ENTRADA' THEN l.valor ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN l.tipo = 'SAIDA' THEN l.valor ELSE 0 END), 0) " +
            "FROM LancamentoCaixa l WHERE l.dataHora <= ?1")
    BigDecimal calcularSaldoAteData(LocalDateTime data);

    @Query("SELECT COALESCE(SUM(CASE WHEN l.tipo = 'ENTRADA' THEN l.valor ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN l.tipo = 'SAIDA' THEN l.valor ELSE 0 END), 0) " +
            "FROM LancamentoCaixa l")
    BigDecimal calcularSaldoTotal();
}