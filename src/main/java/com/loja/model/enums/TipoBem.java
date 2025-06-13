package com.loja.model.enums;

public enum TipoBem {
    IMOVEL("Imóvel"),
    VEICULO("Veículo"),
    MAQUINA("Máquina"),
    UTENSILIO("Utensílio"),
    MOVEIS_E_UTENSILIOS("Móveis e Utensílios"),
    OUTROS("Outros");

    private final String descricao;

    TipoBem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}