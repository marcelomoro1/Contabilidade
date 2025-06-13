package com.loja.repository;

import com.loja.model.Compra;
import com.loja.model.enums.FormaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    /**
     * Busca todas as compras por uma determinada forma de pagamento.
     * @param formaPagamento A forma de pagamento (AVISTA, APRAZO).
     * @return Uma lista de compras.
     */
    List<Compra> findByFormaPagamento(FormaPagamento formaPagamento);

    /**
     * Conta o número de compras em um período específico.
     * @param inicio Data e hora de início do período.
     * @param fim Data e hora de fim do período.
     * @return O número de compras no período.
     */
    @Query("SELECT COUNT(c) FROM Compra c WHERE c.dataCompra BETWEEN :inicio AND :fim")
    Long countByDataCompraBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /**
     * Calcula a soma total dos valores de compra em um período específico.
     * @param inicio Data e hora de início do período.
     * @param fim Data e hora de fim do período.
     * @return A soma total dos valores de compra no período.
     */
    @Query("SELECT SUM(c.valorTotal) FROM Compra c WHERE c.dataCompra BETWEEN :inicio AND :fim")
    BigDecimal sumValorTotalByDataCompraBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}