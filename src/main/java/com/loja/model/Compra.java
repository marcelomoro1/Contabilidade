package com.loja.model;

import com.loja.model.enums.FormaPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCompra> itensCompra = new ArrayList<>();

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "data_compra", nullable = false)
    private LocalDateTime dataCompra;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private Integer parcelas; // Total de parcelas

    private Integer parcelasPagas; // Quantidade de parcelas já pagas
    private BigDecimal valorParcela; // Valor de cada parcela (se a prazo)

    public Compra() {
        this.itensCompra = new ArrayList<>();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }
    public List<ItemCompra> getItensCompra() { return itensCompra; }
    public void setItensCompra(List<ItemCompra> itensCompra) { this.itensCompra = itensCompra; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public LocalDateTime getDataCompra() { return dataCompra; }
    public void setDataCompra(LocalDateTime dataCompra) { this.dataCompra = dataCompra; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }
    public Integer getParcelasPagas() { return parcelasPagas; }
    public void setParcelasPagas(Integer parcelasPagas) { this.parcelasPagas = parcelasPagas; }
    public BigDecimal getValorParcela() { return valorParcela; }
    public void setValorParcela(BigDecimal valorParcela) { this.valorParcela = valorParcela; }

    public void calculateTotalValue() {
        if (itensCompra == null || itensCompra.isEmpty()) {
            this.valorTotal = BigDecimal.ZERO;
            return;
        }
        this.valorTotal = itensCompra.stream()
                .map(ItemCompra::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getSaldoDevedor() {
        if (this.valorTotal == null) {
            return BigDecimal.ZERO;
        }

        if (this.formaPagamento == FormaPagamento.AVISTA) {
            return BigDecimal.ZERO;
        }

        if (this.parcelas == null || this.parcelas <= 0 || this.valorParcela == null) {
            return this.valorTotal;
        }

        // Se todas as parcelas foram pagas, saldo é zero
        if (this.parcelasPagas != null && this.parcelasPagas >= this.parcelas) {
            return BigDecimal.ZERO;
        }

        // Calcular valor total pago
        BigDecimal valorTotalPago = BigDecimal.ZERO;
        if (this.parcelasPagas != null && this.parcelasPagas > 0) {
            // Para as primeiras parcelas, usar o valor base
            int parcelasPagasBase = this.parcelasPagas;
            valorTotalPago = this.valorParcela.multiply(BigDecimal.valueOf(parcelasPagasBase));
        }

        BigDecimal saldo = this.valorTotal.subtract(valorTotalPago);
        return saldo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : saldo;
    }

    @Transient
    public BigDecimal getValorTotalPago() {
        if (this.valorParcela == null || this.parcelasPagas == null) {
            return BigDecimal.ZERO;
        }
        return this.valorParcela.multiply(BigDecimal.valueOf(this.parcelasPagas));
    }

    @Transient
    public BigDecimal getValorProximaParcela() {
        if (this.formaPagamento != FormaPagamento.APRAZO || this.valorParcela == null || this.parcelas == null || this.parcelasPagas == null) {
            return this.valorParcela;
        }
        
        // Se for a última parcela, ajusta para cobrir a diferença
        if (this.parcelasPagas + 1 == this.parcelas) {
            // Calcula o valor total das parcelas já pagas
            BigDecimal valorParcelasPagas = this.valorParcela.multiply(BigDecimal.valueOf(this.parcelasPagas));
            // A última parcela é o que falta para completar o valor total
            BigDecimal valorUltimaParcela = this.valorTotal.subtract(valorParcelasPagas);
            
            // Log para debug
            System.out.println("=== DEBUG VALOR PRÓXIMA PARCELA ===");
            System.out.println("Parcelas Pagas: " + this.parcelasPagas);
            System.out.println("Total de Parcelas: " + this.parcelas);
            System.out.println("É última parcela: " + (this.parcelasPagas + 1 == this.parcelas));
            System.out.println("Valor Parcela Base: " + this.valorParcela);
            System.out.println("Valor Total: " + this.valorTotal);
            System.out.println("Valor Parcelas Pagas: " + valorParcelasPagas);
            System.out.println("Valor Última Parcela: " + valorUltimaParcela);
            System.out.println("===================================");
            
            return valorUltimaParcela;
        }
        
        return this.valorParcela;
    }
}