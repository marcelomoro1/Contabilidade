package com.loja.repository;

import com.loja.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {
    
    @Query("SELECT COUNT(v) FROM Venda v WHERE DATE(v.dataVenda) = :data")
    Long countByDataVenda(@Param("data") LocalDate data);
    
    @Query("SELECT SUM(v.valorTotal) FROM Venda v WHERE DATE(v.dataVenda) = :data")
    Double sumValorTotalByDataVenda(@Param("data") LocalDate data);

    List<Venda> findByDataVendaBetweenOrderByDataVendaDesc(LocalDateTime inicio, LocalDateTime fim);
} 