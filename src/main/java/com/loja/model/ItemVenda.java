package com.loja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode; // Importar para arredondamento

@Entity
public class ItemVenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    private Integer quantidade;

    @Column(name = "preco_unitario", precision = 10, scale = 2) // Preço do produto no momento da venda
    private BigDecimal precoUnitario;

    // --- Campos de Cálculo, se você decidir persistí-los ---
    // @Column(name = "subtotal_item", precision = 10, scale = 2)
    // private BigDecimal subtotal; // Se for persistido
    // @Column(name = "lucro_item", precision = 10, scale = 2)
    // private BigDecimal lucroItem; // Se for persistido
    // --- Fim dos Campos de Cálculo ---

    // Construtor padrão
    public ItemVenda() {
        this.quantidade = 0;
        this.precoUnitario = BigDecimal.ZERO;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    // --- Métodos Calculados (Transientes) ---
    // Subtotal do item (quantidade * precoUnitario)
    // Se você tem um campo 'subtotal' persistido, então este método seria renomeado
    // ou o campo seria removido para que este método seja a fonte do subtotal.
    public BigDecimal getSubtotal() {
        if (this.precoUnitario != null && this.quantidade != null) {
            return this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // Lucro por item (precoVenda - precoCusto) * quantidade
    // Assumindo que 'produto.getPrecoCompra()' é o custo do produto
    // Se você tem um campo 'lucroItem' persistido, mesma lógica do subtotal.
    public BigDecimal getLucroItem() {
        if (this.produto != null && this.produto.getPrecoCompra() != null && this.precoUnitario != null && this.quantidade != null) {
            // PrecoUnitario (preço de venda do item) - PrecoCompra (custo do produto)
            return (this.precoUnitario.subtract(this.produto.getPrecoCompra()))
                    .multiply(BigDecimal.valueOf(this.quantidade))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}