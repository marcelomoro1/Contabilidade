package com.loja.service;

import com.loja.dto.BalancoPatrimonialDTO;

public interface RelatorioService {
    /**
     * Gera o Balanço Patrimonial da loja.
     * Este método agrega dados de diversas entidades para calcular ativos, passivos e patrimônio líquido.
     * @return Um objeto BalancoPatrimonialDTO contendo os dados do balanço.
     */

    BalancoPatrimonialDTO generarBalancoPatrimonial(); // Adicione esta linha!

    // Você pode adicionar outros métodos de relatório aqui no futuro, como DRE, Fluxo de Caixa, etc.
}