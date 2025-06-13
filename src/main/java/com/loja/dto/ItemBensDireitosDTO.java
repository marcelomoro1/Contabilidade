package com.loja.dto;

import java.math.BigDecimal;

public class ItemBensDireitosDTO {
    private String nome;
    private String descricao; // Descrição opcional, se relevante
    private BigDecimal valor;

    public ItemBensDireitosDTO(String nome, BigDecimal valor) {
        this.nome = nome;
        this.valor = valor;
    }

    public ItemBensDireitosDTO(String nome, String descricao, BigDecimal valor) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
    }

    // Construtor padrão para JPA/Jackson, se necessário
    public ItemBensDireitosDTO() {
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}