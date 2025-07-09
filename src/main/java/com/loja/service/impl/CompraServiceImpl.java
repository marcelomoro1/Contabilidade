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
            Produto produto = null;
            if (item.getProduto() != null && item.getProduto().getId() != null) {
                produto = produtoRepository.findById(item.getProduto().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ao salvar compra: " + item.getProduto().getId()));
                item.setProduto(produto);
            } else {
                throw new IllegalArgumentException("Produto não selecionado para um dos itens da compra.");
            }
            item.setValorTotal(item.getPrecoUnitarioCompra().multiply(BigDecimal.valueOf(item.getQuantidade())));
            totalItens = totalItens.add(item.getValorTotal());

            // Calcular crédito de ICMS (17% do valor total do item)
            BigDecimal creditoIcms = item.getPrecoUnitarioCompra().multiply(BigDecimal.valueOf(item.getQuantidade())).multiply(BigDecimal.valueOf(0.17));
            item.setCreditoIcms(creditoIcms);

            produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
            produtoRepository.save(produto);
        }

        compra.setValorTotal(totalItens);

        if (compra.getFormaPagamento() == FormaPagamento.APRAZO) {
            if (compra.getParcelas() == null || compra.getParcelas() <= 0) {
                throw new IllegalArgumentException("Número de parcelas inválido para compra a prazo. Deve ser maior que zero.");
            }
            
            // Cálculo das parcelas
            BigDecimal valorParcelaBase = compra.getValorTotal().divide(BigDecimal.valueOf(compra.getParcelas()), 2, BigDecimal.ROUND_DOWN);
            BigDecimal valorTotalParcelasBase = valorParcelaBase.multiply(BigDecimal.valueOf(compra.getParcelas()));
            BigDecimal diferenca = compra.getValorTotal().subtract(valorTotalParcelasBase);
            
            // Salva o valor base como valor da parcela padrão
            compra.setValorParcela(valorParcelaBase);
            compra.setParcelasPagas(0);
            
            // Log para debug
            System.out.println("=== DEBUG CÁLCULO PARCELAS ===");
            System.out.println("Valor Total: " + compra.getValorTotal());
            System.out.println("Número de Parcelas: " + compra.getParcelas());
            System.out.println("Valor Parcela Base: " + valorParcelaBase);
            System.out.println("Valor Total Parcelas Base: " + valorTotalParcelasBase);
            System.out.println("Diferença: " + diferenca);
            System.out.println("Valor Última Parcela: " + valorParcelaBase.add(diferenca));
            System.out.println("=============================");
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

        if (compra.getSaldoDevedor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Esta compra já está totalmente paga.");
        }

        if (valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a pagar deve ser maior que zero.");
        }

        // Verificar se o valor pago não excede o saldo devedor
        if (valorPago.compareTo(compra.getSaldoDevedor()) > 0) {
            throw new IllegalArgumentException("O valor pago não pode exceder o saldo devedor de " + compra.getSaldoDevedor());
        }

        // Calcular quantas parcelas o valor pago representa
        BigDecimal valorTotalPagoAteAgora = compra.getValorParcela().multiply(BigDecimal.valueOf(compra.getParcelasPagas() != null ? compra.getParcelasPagas() : 0));
        BigDecimal novoValorTotalPago = valorTotalPagoAteAgora.add(valorPago);
        
        // Calcular quantas parcelas foram pagas no total
        int novasParcelasPagas = novoValorTotalPago.divide(compra.getValorParcela(), BigDecimal.ROUND_DOWN).intValue();
        
        // Garantir que não exceda o número total de parcelas
        novasParcelasPagas = Math.min(compra.getParcelas(), novasParcelasPagas);
        
        compra.setParcelasPagas(novasParcelasPagas);
        
        // Se o saldo devedor for zero ou negativo após o pagamento, marcar como totalmente paga
        BigDecimal saldoAposPagamento = compra.getValorTotal().subtract(novoValorTotalPago);
        if (saldoAposPagamento.compareTo(BigDecimal.ZERO) <= 0) {
            compra.setParcelasPagas(compra.getParcelas());
        }

        compraRepository.save(compra);
        
        // Log para debug
        System.out.println("=== DEBUG PAGAMENTO ===");
        System.out.println("Valor Pago: " + valorPago);
        System.out.println("Valor Total Pago Até Agora: " + novoValorTotalPago);
        System.out.println("Novas Parcelas Pagas: " + novasParcelasPagas);
        System.out.println("Saldo Após Pagamento: " + saldoAposPagamento);
        System.out.println("Saldo Devedor Final: " + compra.getSaldoDevedor());
        System.out.println("=======================");
    }
}