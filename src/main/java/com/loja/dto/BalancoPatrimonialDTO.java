package com.loja.dto; // Crie um pacote 'dto' para isso

import java.math.BigDecimal;
import java.util.ArrayList; // Importar para inicializar listas
import java.util.List;

public class BalancoPatrimonialDTO {
    private BigDecimal totalAtivos;
    private BigDecimal totalPassivos;
    private BigDecimal patrimonioLiquido;
    private List<ItemBensDireitosDTO> bensDireitos; // Lista de ativos detalhados
    private List<ItemObrigacoesDTO> obrigacoes;     // Lista de passivos detalhados
    private List<ItemBensDireitosDTO> patrimonioLiquidoItems;


    public BalancoPatrimonialDTO() {
        this.totalAtivos = BigDecimal.ZERO;
        this.totalPassivos = BigDecimal.ZERO;
        this.patrimonioLiquido = BigDecimal.ZERO;
        this.bensDireitos = new ArrayList<>();
        this.obrigacoes = new ArrayList<>();
        this.patrimonioLiquidoItems = new ArrayList<>();
    }

    // Construtor com parâmetros (opcional, para conveniência ao construir o DTO)
    public BalancoPatrimonialDTO(BigDecimal totalAtivos, BigDecimal totalPassivos, BigDecimal patrimonioLiquido,
                                 List<ItemBensDireitosDTO> bensDireitos, List<ItemObrigacoesDTO> obrigacoes,
                                 List<ItemBensDireitosDTO> patrimonioLiquidoItems) {
        this.totalAtivos = totalAtivos;
        this.totalPassivos = totalPassivos;
        this.patrimonioLiquido = patrimonioLiquido;
        this.bensDireitos = bensDireitos;
        this.obrigacoes = obrigacoes;
        this.patrimonioLiquidoItems = patrimonioLiquidoItems;
    }


    // Getters e Setters
    public BigDecimal getTotalAtivos() { return totalAtivos; }
    public void setTotalAtivos(BigDecimal totalAtivos) { this.totalAtivos = totalAtivos; }
    public BigDecimal getTotalPassivos() { return totalPassivos; }
    public void setTotalPassivos(BigDecimal totalPassivos) { this.totalPassivos = totalPassivos; }
    public BigDecimal getPatrimonioLiquido() { return patrimonioLiquido; }
    public void setPatrimonioLiquido(BigDecimal patrimonioLiquido) { this.patrimonioLiquido = patrimonioLiquido; }
    public List<ItemBensDireitosDTO> getBensDireitos() { return bensDireitos; }
    public void setBensDireitos(List<ItemBensDireitosDTO> bensDireitos) { this.bensDireitos = bensDireitos; }
    public List<ItemObrigacoesDTO> getObrigacoes() { return obrigacoes; }
    public void setObrigacoes(List<ItemObrigacoesDTO> obrigacoes) { this.obrigacoes = obrigacoes; }
    public List<ItemBensDireitosDTO> getPatrimonioLiquidoItems() { return patrimonioLiquidoItems; }
    public void setPatrimonioLiquidoItems(List<ItemBensDireitosDTO> patrimonioLiquidoItems) { this.patrimonioLiquidoItems = patrimonioLiquidoItems; }
}