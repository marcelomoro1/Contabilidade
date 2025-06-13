package com.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.io.Serializable; // Boa prática para entidades JPA

@Entity
@Table(name = "patrimonio")
public class Patrimonio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID deve ser Long para GenerationType.IDENTITY

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    // A quantidade pode ser útil para itens como "cadeiras", "mesas" etc.
    // Se for um item único (ex: "Sede da Loja"), a quantidade seria 1.
    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 12, scale = 2) // Maior precisão para valor monetário
    private BigDecimal valorUnitario; // Valor unitário do bem

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal; // Valor total (quantidade * valorUnitario)

    // Construtor padrão (necessário para JPA)
    public Patrimonio() {
        this.quantidade = 0;
        this.valorUnitario = BigDecimal.ZERO;
        this.valorTotal = BigDecimal.ZERO;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
        // Recalcula o valor total ao definir o valor unitário
        if (this.quantidade != null) {
            this.valorTotal = valorUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        }
    }
    public BigDecimal getValorTotal() {
        // Garante que o valor total seja sempre calculado/atualizado
        if (this.valorUnitario != null && this.quantidade != null) {
            return this.valorUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        }
        return valorTotal; // Retorna o valor armazenado se as bases não existirem
    }
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    } // Setter para JPA, mas o getter é o preferencial para cálculo
}