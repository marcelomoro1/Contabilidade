package com.trabalho.apihamburgueria.repository;

import com.trabalho.apihamburgueria.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

}
