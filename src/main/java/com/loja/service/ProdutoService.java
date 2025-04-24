package com.loja.service;

import com.loja.model.Produto;
import com.loja.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto salvar(Produto produto) {
        calcularCusto(produto);
        calcularLucro(produto);
        return produtoRepository.save(produto);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    public void deletar(Long id) {
        produtoRepository.deleteById(id);
    }

    public Long countProdutosEmEstoque() {
        return produtoRepository.countByQuantidadeGreaterThan(0);
    }

    private void calcularCusto(Produto produto) {
        BigDecimal custoBase = produto.getPrecoCompra()
                .multiply(BigDecimal.valueOf(produto.getQuantidade()));
        
        // Calcula o valor do ICMS com base no custo base
        BigDecimal debitoIcms = BigDecimal.ZERO;
        if (produto.getDebitoIcms() != null) {
            debitoIcms = custoBase.multiply(produto.getDebitoIcms())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        
        BigDecimal creditoIcms = BigDecimal.ZERO;
        if (produto.getCreditoIcms() != null) {
            creditoIcms = custoBase.multiply(produto.getCreditoIcms())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        
        BigDecimal custoTotal = custoBase.add(debitoIcms).subtract(creditoIcms);
        produto.setCustoTotal(custoTotal);
    }

    private void calcularLucro(Produto produto) {
        BigDecimal valorVenda = produto.getPrecoVenda()
                .multiply(BigDecimal.valueOf(produto.getQuantidade()));
        
        BigDecimal lucroTotal = valorVenda.subtract(produto.getCustoTotal());
        produto.setLucroTotal(lucroTotal);
    }
} 