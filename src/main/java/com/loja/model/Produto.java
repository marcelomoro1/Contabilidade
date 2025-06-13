package com.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.io.Serializable;

@Entity
@Table(name = "produtos")
public class Produto implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;

    @Column(name = "preco_compra", precision = 10, scale = 2) // Adicionado precisão
    private BigDecimal precoCompra;

    @Column(name = "preco_venda", precision = 10, scale = 2) // Adicionado precisão
    private BigDecimal precoVenda;

    @Column(name = "credito_icms", precision = 10, scale = 2) // Adicionado precisão
    private BigDecimal creditoIcms;

    @Column(name = "debito_icms", precision = 10, scale = 2) // Adicionado precisão
    private BigDecimal debitoIcms;

    private Integer quantidade;

    @Column(name = "custo_total", precision = 10, scale = 2) // Adicionado precisão
    private BigDecimal custoTotal;

    @Column(name = "lucro_total", precision = 10, scale = 2) // Adicionado precisão
    private BigDecimal lucroTotal;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public BigDecimal getCreditoIcms() {
        return creditoIcms;
    }

    public void setCreditoIcms(BigDecimal creditoIcms) {
        this.creditoIcms = creditoIcms;
    }

    public BigDecimal getDebitoIcms() {
        return debitoIcms;
    }

    public void setDebitoIcms(BigDecimal debitoIcms) {
        this.debitoIcms = debitoIcms;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public BigDecimal getLucroTotal() {
        return lucroTotal;
    }

    public void setLucroTotal(BigDecimal lucroTotal) {
        this.lucroTotal = lucroTotal;
    }
}