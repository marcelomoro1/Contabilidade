package com.loja.dto;

import java.math.BigDecimal;

public class ItemObrigacoesDTO {
    private String descricao;
    private BigDecimal valor;

    public ItemObrigacoesDTO(String descricao, BigDecimal valor) {
        this.descricao = descricao;
        this.valor = valor;
    }


    public ItemObrigacoesDTO() {
    }

    // Getters e Setters
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}