package com.loja.service;

import com.loja.model.CapitalSocial;

import java.util.List;
import java.util.Optional;

public interface CapitalSocialService {
    /**
     * Salva um registro de Capital Social.
     * @param capitalSocial O objeto CapitalSocial a ser salvo.
     * @return O CapitalSocial salvo.
     */
    CapitalSocial salvar(CapitalSocial capitalSocial);

    /**
     * Busca o último registro de Capital Social por data de abertura.
     * @return Um Optional contendo o último CapitalSocial, ou vazio se não houver nenhum.
     */
    Optional<CapitalSocial> buscarUltimoCapitalSocial();

    /**
     * Lista todos os registros de Capital Social.
     * @return Uma lista de todos os registros de Capital Social.
     */
    List<CapitalSocial> listarTodos();

    /**
     * Busca um registro de Capital Social pelo ID.
     * @param id O ID do registro de Capital Social.
     * @return O CapitalSocial encontrado.
     * @throws RuntimeException se o registro não for encontrado.
     */
    CapitalSocial buscarPorId(Long id);
}