package com.loja.model;

import com.loja.model.enums.TipoBem; // Nova enum
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate; // Importar LocalDate

@Entity
@Table(name = "bens")
public class Bem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2) // Adicionar precisão e escala
    private BigDecimal valorAquisicao;

    @Column(nullable = false)
    private LocalDate dataAquisicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBem tipoBem;

    // Campos para controle de contas a pagar de bens (se for a prazo)
    @Column(precision = 10, scale = 2) // Adicionar precisão e escala
    private BigDecimal valorEntrada;
    private Integer parcelasTotais;
    private Integer parcelasPagas;
    @Column(precision = 10, scale = 2) // Adicionar precisão e escala
    private BigDecimal valorParcela;
    private LocalDate dataVencimentoPrimeiraParcela;
    private String fornecedorNome;
    private Long fornecedorId;

    // Construtor padrão para inicializar valores padrão e evitar NullPointerExceptions
    public Bem() {
        this.valorAquisicao = BigDecimal.ZERO;
        this.valorEntrada = BigDecimal.ZERO;
        this.parcelasTotais = 0;
        this.parcelasPagas = 0;
        this.valorParcela = BigDecimal.ZERO;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValorAquisicao() { return valorAquisicao; }
    public void setValorAquisicao(BigDecimal valorAquisicao) { this.valorAquisicao = valorAquisicao; }
    public LocalDate getDataAquisicao() { return dataAquisicao; }
    public void setDataAquisicao(LocalDate dataAquisicao) { this.dataAquisicao = dataAquisicao; }
    public TipoBem getTipoBem() { return tipoBem; }
    public void setTipoBem(TipoBem tipoBem) { this.tipoBem = tipoBem; }
    public BigDecimal getValorEntrada() { return valorEntrada; }
    public void setValorEntrada(BigDecimal valorEntrada) { this.valorEntrada = valorEntrada; }
    public Integer getParcelasTotais() { return parcelasTotais; }
    public void setParcelasTotais(Integer parcelasTotais) { this.parcelasTotais = parcelasTotais; }
    public Integer getParcelasPagas() { return parcelasPagas; }
    public void setParcelasPagas(Integer parcelasPagas) { this.parcelasPagas = parcelasPagas; }
    public BigDecimal getValorParcela() { return valorParcela; }
    public void setValorParcela(BigDecimal valorParcela) { this.valorParcela = valorParcela; }
    public LocalDate getDataVencimentoPrimeiraParcela() { return dataVencimentoPrimeiraParcela; }
    public void setDataVencimentoPrimeiraParcela(LocalDate dataVencimentoPrimeiraParcela) { this.dataVencimentoPrimeiraParcela = dataVencimentoPrimeiraParcela; }
    public String getFornecedorNome() { return fornecedorNome; }
    public void setFornecedorNome(String fornecedorNome) { this.fornecedorNome = fornecedorNome; }
    public Long getFornecedorId() { return fornecedorId; }
    public void setFornecedorId(Long fornecedorId) { this.fornecedorId = fornecedorId; }

    // Método auxiliar para calcular o saldo devedor
    @Transient // Indica que este campo não será persistido no banco de dados
    public BigDecimal getSaldoDevedor() {
        if (parcelasTotais == null || parcelasPagas == null || valorParcela == null) {
            return BigDecimal.ZERO;
        }
        // Garante que a diferença não é negativa
        int parcelasRestantes = Math.max(0, parcelasTotais - parcelasPagas);
        return valorParcela.multiply(BigDecimal.valueOf(parcelasRestantes));
    }
}