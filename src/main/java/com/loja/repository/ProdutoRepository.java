package com.loja.repository;

import com.loja.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    Long countByQuantidadeGreaterThan(Integer quantidade);
} 