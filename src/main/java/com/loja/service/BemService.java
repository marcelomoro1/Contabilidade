package com.loja.service;

import com.loja.model.Bem;

import java.math.BigDecimal;
import java.util.List;

public interface BemService {
    /**
     * Salva um bem (Patrimônio).
     * @param bem O objeto Bem a ser salvo.
     * @return O Bem salvo.
     */
    Bem salvar(Bem bem);

    /**
     * Lista todos os bens (Patrimônio).
     * @return Uma lista de todos os bens.
     */
    List<Bem> listarTodos();

    /**
     * Busca um bem pelo ID.
     * @param id O ID do bem.
     * @return O Bem encontrado.
     * @throws RuntimeException se o bem não for encontrado.
     */
    Bem buscarPorId(Long id);

    /**
     * Deleta um bem pelo ID.
     * @param id O ID do bem a ser deletado.
     */
    void deletar(Long id);

    /**
     * Registra o pagamento de uma parcela de um bem adquirido a prazo.
     * @param bemId O ID do bem.
     * @param valorPago O valor pago (pode ser usado para validações ou lógica de pagamento parcial).
     * @throws RuntimeException se o bem não for encontrado ou não houver parcelas pendentes.
     */
    void registrarPagamentoParcela(Long bemId, BigDecimal valorPago);
}