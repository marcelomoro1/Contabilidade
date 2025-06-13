package com.loja.service;

import com.loja.model.Produto;
import java.util.List;

public interface ProdutoService {
    /**
     * Lista todos os produtos cadastrados.
     * @return Uma lista de objetos Produto.
     */
    List<Produto> listarTodos();

    /**
     * Busca um produto pelo seu ID.
     * @param id O ID do produto a ser buscado.
     * @return O objeto Produto correspondente ao ID.
     * @throws RuntimeException se o produto não for encontrado.
     */
    Produto buscarPorId(Long id);

    /**
     * Salva ou atualiza um produto.
     * @param produto O objeto Produto a ser salvo.
     * @return O objeto Produto salvo/atualizado.
     */
    Produto salvar(Produto produto);

    /**
     * Deleta um produto pelo seu ID.
     * @param id O ID do produto a ser deletado.
     */
    void deletar(Long id);

    /**
     * Conta o número total de produtos em estoque.
     * @return O número total de produtos com quantidade maior que zero.
     */
    long countProdutosEmEstoque();
}