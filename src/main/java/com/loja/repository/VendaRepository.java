package com.loja.repository;

import com.loja.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByDataVendaBetweenOrderByDataVendaDesc(LocalDateTime inicio, LocalDateTime fim);

    List<Venda> findByFormaPagamento(com.loja.model.enums.FormaPagamento formaPagamento); // Para contas a receber

    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
    Long countByDataVendaBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT SUM(v.valorTotal) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
    BigDecimal sumValorTotalByDataVendaBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Método que você já tinha no service, pode ser implementado assim no repo
    default Long countByDataVenda(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(23, 59, 59);
        return countByDataVendaBetween(inicioDia, fimDia);
    }

    // Método que você já tinha no service, pode ser implementado assim no repo
    default Double sumValorTotalByDataVenda(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(23, 59, 59);
        BigDecimal sum = sumValorTotalByDataVendaBetween(inicioDia, fimDia);
        return sum != null ? sum.doubleValue() : null;
    }
}