package com.loja.repository;

import com.loja.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    /**
     * Conta o número de produtos cuja quantidade em estoque é maior que um determinado valor.
     * @param quantidade O valor mínimo da quantidade em estoque.
     * @return O número de produtos que atendem ao critério.
     */
    long countByQuantidadeGreaterThan(Integer quantidade);
}