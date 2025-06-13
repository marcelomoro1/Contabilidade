package com.loja.repository;

import com.loja.model.Bem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importação adicionada
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // Importação adicionada
import java.util.List;

@Repository
public interface BemRepository extends JpaRepository<Bem, Long> {
    // **NOVO: Método para somar o valor de aquisição de todos os bens.**
    // COALESCE(SUM(b.valorAquisicao), 0) garante que, se não houver bens, retorne 0 ao invés de null.
    @Query("SELECT COALESCE(SUM(b.valorAquisicao), 0) FROM Bem b")
    BigDecimal sumValorAquisicaoTotal();
}