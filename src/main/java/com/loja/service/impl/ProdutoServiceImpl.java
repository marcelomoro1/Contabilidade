package com.loja.service.impl;

import com.loja.model.Produto;
import com.loja.repository.ProdutoRepository; // Certifique-se de ter este repositório
import com.loja.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    @Override
    @Transactional
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProdutosEmEstoque() {
        return produtoRepository.countByQuantidadeGreaterThan(0);
    }
}