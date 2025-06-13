package com.loja.model.enums;

public enum FormaPagamento {
    AVISTA("À Vista"),
    APRAZO("A Prazo");
    // Você pode adicionar outras formas de pagamento se necessário, seguindo o padrão

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}