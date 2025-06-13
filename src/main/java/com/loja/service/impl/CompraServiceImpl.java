package com.loja.service.impl;

import com.loja.model.Compra;
import com.loja.model.ItemCompra;
import com.loja.model.Produto;
import com.loja.model.enums.FormaPagamento;
import com.loja.repository.CompraRepository;
import com.loja.repository.ProdutoRepository;
import com.loja.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraServiceImpl implements CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Compra salvar(Compra compra) {
        if (compra.getId() == null) {
            compra.setDataCompra(LocalDateTime.now());
        }

        BigDecimal totalItens = BigDecimal.ZERO;
        if (compra.getItensCompra() == null || compra.getItensCompra().isEmpty()) {
            throw new IllegalArgumentException("A compra deve ter pelo menos um item.");
        }

        for (ItemCompra item : compra.getItensCompra()) {
            item.setCompra(compra);
            item.setValorTotal(item.getPrecoUnitarioCompra().multiply(BigDecimal.valueOf(item.getQuantidade())));
            totalItens = totalItens.add(item.getValorTotal());

            Produto produto = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ao salvar compra: " + item.getProduto().getId()));

            // --- CORREÇÃO AQUI: USANDO getQuantidade() e setQuantidade() ---
            produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        compra.setValorTotal(totalItens);

        if (compra.getFormaPagamento() == FormaPagamento.APRAZO) {
            if (compra.getParcelas() == null || compra.getParcelas() <= 0) {
                throw new IllegalArgumentException("Número de parcelas inválido para compra a prazo. Deve ser maior que zero.");
            }
            compra.setValorParcela(compra.getValorTotal().divide(BigDecimal.valueOf(compra.getParcelas()), BigDecimal.ROUND_HALF_UP));
            compra.setParcelasPagas(0);
        } else { // À VISTA
            compra.setParcelas(1);
            compra.setParcelasPagas(1);
            compra.setValorParcela(compra.getValorTotal());
        }

        return compraRepository.save(compra);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra não encontrada para exclusão: " + id));

        compra.getItensCompra().forEach(item -> {
            Produto produto = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ao reverter estoque para exclusão da compra: " + item.getProduto().getId()));

            // --- CORREÇÃO AQUI: USANDO getQuantidade() e setQuantidade() ---
            produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
            produtoRepository.save(produto);
        });
        compraRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Compra buscarPorId(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra não encontrada com o ID: " + id));
    }

    @Override
    @Transactional
    public void registrarPagamento(Long compraId, BigDecimal valorPago) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra não encontrada com o ID: " + compraId));

        if (compra.getFormaPagamento() != FormaPagamento.APRAZO) {
            throw new IllegalArgumentException("Não é possível registrar pagamento para uma compra que não é a prazo.");
        }

        // Usar o getter para verificar o saldo devedor atual
        if (compra.getSaldoDevedor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Esta compra já está totalmente paga.");
        }

        if (valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a pagar deve ser maior que zero.");
        }

        // Calcula o valor total já pago (considerando pagamentos anteriores)
        BigDecimal valorTotalPagoAteAgora = compra.getValorTotal().subtract(compra.getSaldoDevedor());
        BigDecimal novoValorTotalPago = valorTotalPagoAteAgora.add(valorPago);

        // Se a compra tem parcelas definidas (e valorParcela)
        if (compra.getValorParcela() != null && compra.getValorParcela().compareTo(BigDecimal.ZERO) > 0 && compra.getParcelas() != null && compra.getParcelas() > 0) {
            int novasParcelasPagas = novoValorTotalPago.divide(compra.getValorParcela(), BigDecimal.ROUND_DOWN).intValue();

            compra.setParcelasPagas(Math.min(compra.getParcelas(), novasParcelasPagas));
        } else {
            // Caso de fallback: Se o pagamento for suficiente para cobrir o valor total, marca todas as parcelas como pagas.
            if (compra.getSaldoDevedor().compareTo(valorPago) <= 0) {
                compra.setParcelasPagas(compra.getParcelas());
            }
        }

        compraRepository.save(compra);
    }
}