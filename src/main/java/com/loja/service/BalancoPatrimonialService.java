package com.loja.service;

import com.loja.dto.BalancoPatrimonialDTO;
import java.time.LocalDate; // Importar LocalDate

public interface BalancoPatrimonialService {

    // Método original (se ainda for usado em algum lugar)
    BalancoPatrimonialDTO gerarBalanco();

    // **NOVO: Adicionar este método à interface**
    BalancoPatrimonialDTO gerarBalancoPorData(LocalDate dataReferencia);
}