package com.loja.service.impl;

import com.loja.model.Produto;
import com.loja.model.Venda;
import com.loja.model.ItemVenda;
import com.loja.repository.VendaRepository;
import com.loja.repository.ProdutoRepository;
import com.loja.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode; // Importar RoundingMode

import java.util.List;

@Service
public class VendaServiceImpl implements VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Venda salvar(Venda venda) {
        // A dataVenda já está sendo setada no construtor de Venda ou no Controller.
        // Se a venda já tem ID, significa que é uma atualização e a data não deve ser alterada.
        if (venda.getId() == null && venda.getDataVenda() == null) {
            venda.setDataVenda(LocalDateTime.now());
        }

        // Garante a associação bidirecional e calcula o valor total dos itens
        // O valorTotal da Venda agora é calculado pelo getter Venda.getValorTotal()
        // que soma os subtotais dos itens.
        // Não precisamos mais de `totalItens` no Service para setar o valorTotal da Venda.

        // Validação e atualização de estoque para CADA item da venda
        if (venda.getItensVenda().isEmpty()) {
            throw new IllegalArgumentException("A venda deve conter pelo menos um item.");
        }

        for (ItemVenda item : venda.getItensVenda()) {
            // Garante a associação bidirecional (se já não foi feita no controller ou construtor)
            item.setVenda(venda);

            // Buscar o produto para garantir que está no estado gerenciado e atualizar estoque
            Produto produto = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado ao salvar venda: ID " + item.getProduto().getId()));

            if (produto.getQuantidade() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome() + ". Quantidade em estoque: " + produto.getQuantidade() + ", Quantidade solicitada: " + item.getQuantidade());
            }

            // Atualiza o estoque do produto
            produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
            produtoRepository.save(produto); // Salva a atualização do estoque

            // O preço unitário do item de venda deve ser o preço de venda do produto no momento da venda
            item.setPrecoUnitario(produto.getPrecoVenda());
        }

        // Define o valorTotal da Venda através do getter que calcula a soma dos itens
        // É importante chamar o getter para que o valor seja calculado antes de salvar.
        // Como o getValorTotal() já faz o cálculo, não precisamos de um setValorTotal aqui
        // a não ser que você queira persistir um campo valorTotal (o que já está na sua Venda).
        // Se o @Column na Venda tiver o nome "valor_total", e o getter for getValorTotal(),
        // o JPA vai mapear automaticamente.

        // Lógica para parcelamento
        if (venda.getFormaPagamento() == com.loja.model.enums.FormaPagamento.APRAZO) {
            if (venda.getParcelas() == null || venda.getParcelas() <= 0) {
                throw new IllegalArgumentException("Número de parcelas inválido para venda a prazo.");
            }
            // Valor da parcela calculado com base no valor total da venda
            venda.setValorParcela(venda.getValorTotal().divide(BigDecimal.valueOf(venda.getParcelas()), 2, RoundingMode.HALF_UP));
            venda.setParcelasPagas(0); // Venda a prazo inicia com 0 parcelas pagas
            venda.setSaldoAReceber(venda.getValorTotal()); // Inicializa saldo a receber
        } else {
            venda.setParcelas(1);
            venda.setParcelasPagas(1); // Venda à vista já está paga
            venda.setValorParcela(venda.getValorTotal()); // Parcela única é o valor total
            venda.setSaldoAReceber(BigDecimal.ZERO); // Saldo a receber é zero para venda à vista
        }

        return vendaRepository.save(venda);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada para exclusão: " + id));

        // Reverter estoque ao deletar a venda
        for (ItemVenda item : venda.getItensVenda()) {
            Produto produto = item.getProduto();
            // Garante que o produto existe antes de tentar atualizar o estoque
            produtoRepository.findById(produto.getId()).ifPresent(p -> {
                p.setQuantidade(p.getQuantidade() + item.getQuantidade());
                produtoRepository.save(p);
            });
        }

        vendaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venda> listarTodas() {
        return vendaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venda> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByDataVendaBetweenOrderByDataVendaDesc(inicio, fim);
    }

    @Override
    @Transactional(readOnly = true)
    public Venda buscarPorId(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public Long countVendasByData(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(23, 59, 59);
        return vendaRepository.countByDataVendaBetween(inicioDia, fimDia);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calcularFaturamentoPorData(LocalDate data) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(23, 59, 59);
        BigDecimal faturamento = vendaRepository.sumValorTotalByDataVendaBetween(inicioDia, fimDia);
        return faturamento != null ? faturamento.doubleValue() : 0.0;
    }
}