package com.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class ItemCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private Integer quantidade;

    @Column(name = "preco_unitario_compra")
    private BigDecimal precoUnitarioCompra; // Custo por unidade

    @Column(name = "valor_total")
    private BigDecimal valorTotal; // Quantidade * PrecoUnitarioCompra

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitarioCompra() { return precoUnitarioCompra; }
    public void setPrecoUnitarioCompra(BigDecimal precoUnitarioCompra) { this.precoUnitarioCompra = precoUnitarioCompra; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}