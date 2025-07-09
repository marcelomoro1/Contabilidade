package com.loja.model;

import com.loja.model.enums.FormaPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode; // Importar para arredondamento

@Entity
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    private LocalDateTime dataVenda;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    private Integer parcelas; // Número de parcelas se for a prazo

    @Column(name = "valor_total", precision = 10, scale = 2) // Campo persistido para o total da venda (será populado no service)
    private BigDecimal valorTotal; // Manter como campo, mas seu valor será definido pelo service

    @Column(name = "saldo_a_receber", precision = 10, scale = 2)
    private BigDecimal saldoAReceber;

    // NOVOS CAMPOS PARA PARCELAMENTO
    @Column(name = "valor_parcela", precision = 10, scale = 2)
    private BigDecimal valorParcela;

    @Column(name = "parcelas_pagas")
    private Integer parcelasPagas;
    // FIM DOS NOVOS CAMPOS

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Adicionei LAZY
    private List<ItemVenda> itensVenda = new ArrayList<>();

    // Construtor padrão
    public Venda() {
        this.dataVenda = LocalDateTime.now();
        this.valorTotal = BigDecimal.ZERO;
        this.saldoAReceber = BigDecimal.ZERO;
        this.valorParcela = BigDecimal.ZERO;
        this.parcelasPagas = 0;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDateTime dataVenda) { this.dataVenda = dataVenda; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public Integer getParcelas() { return parcelas; }
    public void setParcelas(Integer parcelas) { this.parcelas = parcelas; }

    // O getter de valorTotal será o campo persistido.
    // O service é responsável por calcular a soma dos itens e setar este campo.
    public BigDecimal getValorTotal() {
        // Se você quer que o getter CALCULE DINAMICAMENTE (e não pegue o campo persistido)
        // e se o campo valorTotal for apenas para persistir o último cálculo, então:
        // return itensVenda.stream()
        //         .map(ItemVenda::getSubtotal)
        //         .reduce(BigDecimal.ZERO, BigDecimal::add)
        //         .setScale(2, RoundingMode.HALF_UP);
        // Ou, se o campo for o valor de referência:
        return valorTotal;
    }
    // O setter para valorTotal é necessário porque o service o preenche.
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }


    public BigDecimal getSaldoAReceber() { return saldoAReceber; }
    public void setSaldoAReceber(BigDecimal saldoAReceber) { this.saldoAReceber = saldoAReceber; }

    public BigDecimal getValorParcela() { return valorParcela; }
    public void setValorParcela(BigDecimal valorParcela) { this.valorParcela = valorParcela; }

    public Integer getParcelasPagas() { return parcelasPagas; }
    public void setParcelasPagas(Integer parcelasPagas) { this.parcelasPagas = parcelasPagas; }

    public List<ItemVenda> getItensVenda() { return itensVenda; }
    public void setItensVenda(List<ItemVenda> itensVenda) { this.itensVenda = itensVenda; }

    // Método auxiliar para adicionar um item à venda
    public void addItemVenda(ItemVenda item) {
        if (this.itensVenda == null) {
            this.itensVenda = new ArrayList<>();
        }
        this.itensVenda.add(item);
        item.setVenda(this); // Garante a ligação bidirecional
    }

    // Método auxiliar para remover um item da venda
    public void removeItemVenda(ItemVenda item) {
        if (this.itensVenda != null) {
            this.itensVenda.remove(item);
            item.setVenda(null);
        }
    }
}