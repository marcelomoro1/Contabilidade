package com.loja.service.impl;

import com.loja.model.Bem;
import com.loja.model.LancamentoCaixa;
import com.loja.model.enums.TipoLancamentoCaixa;
import com.loja.repository.BemRepository;
import com.loja.service.BemService;
import com.loja.service.CaixaService; // Importação adicionada
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime; // Importação adicionada
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BemServiceImpl implements BemService {

    @Autowired
    private BemRepository bemRepository;

    @Autowired
    private CaixaService caixaService;

    @Override
    @Transactional
    public Bem salvar(Bem bem) {
        if (bem.getValorAquisicao() == null) {
            throw new IllegalArgumentException("Valor de aquisição é obrigatório.");
        }
        if (bem.getValorEntrada() == null) {
            bem.setValorEntrada(BigDecimal.ZERO);
        }

        boolean isNewBem = (bem.getId() == null); // Verifica se é um novo bem para evitar lançamentos duplicados ao editar

        // Lógica para bens a prazo
        if (bem.getParcelasTotais() != null && bem.getParcelasTotais() > 0) {
            BigDecimal valorFinanciado = bem.getValorAquisicao().subtract(bem.getValorEntrada());
            if (valorFinanciado.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Valor de entrada não pode ser maior que o valor de aquisição.");
            }
            if (valorFinanciado.compareTo(BigDecimal.ZERO) == 0 && bem.getParcelasTotais() > 0) {
                throw new IllegalArgumentException("Não é possível parcelar se o valor financiado é zero.");
            }
            if (bem.getParcelasTotais() <= 0) {
                throw new IllegalArgumentException("Número de parcelas deve ser maior que zero para parcelamento.");
            }
            bem.setValorParcela(valorFinanciado.divide(BigDecimal.valueOf(bem.getParcelasTotais()), 2, RoundingMode.HALF_UP));
            bem.setParcelasPagas(Objects.requireNonNullElse(bem.getParcelasPagas(), 0));
            if (bem.getParcelasPagas() > bem.getParcelasTotais()) {
                bem.setParcelasPagas(bem.getParcelasTotais());
            }

            //Lançar a saída do valor da ENTRADA no caixa, se for um novo bem e houver entrada
            if (isNewBem && bem.getValorEntrada().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    LancamentoCaixa lancamentoEntradaBem = new LancamentoCaixa();
                    lancamentoEntradaBem.setTipo(TipoLancamentoCaixa.SAIDA);
                    lancamentoEntradaBem.setValor(bem.getValorEntrada());
                    lancamentoEntradaBem.setDescricao("Entrada na aquisição do bem: " + bem.getNome());
                    lancamentoEntradaBem.setDataHora(LocalDateTime.now());
                    caixaService.registrarLancamento(lancamentoEntradaBem);
                    System.out.println("DEBUG: Saída de caixa de R$" + bem.getValorEntrada() + " registrada como entrada para o bem: " + bem.getNome());
                } catch (Exception e) {
                    System.err.println("ERRO: Falha ao registrar a entrada do bem no Caixa: " + e.getMessage());
                    throw new RuntimeException("Erro ao registrar a entrada do bem no Caixa.", e);
                }
            }

        } else { // Tratamento para bens à vista (parcelasTotais 0 ou nulo)
            bem.setParcelasTotais(0);
            bem.setValorParcela(BigDecimal.ZERO);
            bem.setParcelasPagas(0);

            //Lançar a saída do VALOR TOTAL DE AQUISIÇÃO no caixa, se for um novo bem e à vista.
            if (isNewBem && bem.getValorAquisicao().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    LancamentoCaixa lancamentoAquisicaoBem = new LancamentoCaixa();
                    lancamentoAquisicaoBem.setTipo(TipoLancamentoCaixa.SAIDA);
                    lancamentoAquisicaoBem.setValor(bem.getValorAquisicao());
                    lancamentoAquisicaoBem.setDescricao("Aquisição do bem à vista: " + bem.getNome());
                    lancamentoAquisicaoBem.setDataHora(LocalDateTime.now());
                    caixaService.registrarLancamento(lancamentoAquisicaoBem);
                    System.out.println("DEBUG: Saída de caixa de R$" + bem.getValorAquisicao() + " registrada pela aquisição à vista do bem: " + bem.getNome());
                } catch (Exception e) {
                    System.err.println("ERRO: Falha ao registrar a aquisição do bem à vista no Caixa: " + e.getMessage());
                    throw new RuntimeException("Erro ao registrar a aquisição do bem à vista no Caixa.", e);
                }
            }
        }

        return bemRepository.save(bem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bem> listarTodos() {
        return bemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Bem buscarPorId(Long id) {
        return bemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bem não encontrado."));
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        bemRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void registrarPagamentoParcela(Long bemId, BigDecimal valorPago) {
        Bem bem = bemRepository.findById(bemId)
                .orElseThrow(() -> new RuntimeException("Bem não encontrado para registrar pagamento."));

        if (bem.getParcelasTotais() == null || bem.getParcelasTotais() <= 0 || bem.getParcelasPagas() >= bem.getParcelasTotais()) {
            throw new RuntimeException("Este bem não possui parcelas pendentes ou já está totalmente pago.");
        }

        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor pago deve ser positivo.");
        }

        BigDecimal valorParcelaEsperado = bem.getValorParcela();
        if (valorPago.compareTo(valorParcelaEsperado) < 0) {
            throw new IllegalArgumentException("Valor pago é menor que o valor da parcela. Favor pagar o valor exato da parcela.");
        }

        int parcelasPagasNestaTransacao = valorPago.divide(valorParcelaEsperado, 0, RoundingMode.DOWN).intValue();
        if (parcelasPagasNestaTransacao <= 0 && valorPago.compareTo(BigDecimal.ZERO) > 0) {
            parcelasPagasNestaTransacao = 1;
        }

        //Lançar a saída do valor da(s) parcela(s) paga(s) no caixa.
        BigDecimal valorRealSaidaCaixa = valorParcelaEsperado.multiply(BigDecimal.valueOf(parcelasPagasNestaTransacao));
        try {
            LancamentoCaixa lancamentoPagamentoParcela = new LancamentoCaixa();
            lancamentoPagamentoParcela.setTipo(TipoLancamentoCaixa.SAIDA);
            lancamentoPagamentoParcela.setValor(valorRealSaidaCaixa);
            lancamentoPagamentoParcela.setDescricao("Pagamento de parcela(s) do bem: " + bem.getNome() + " (" + parcelasPagasNestaTransacao + " parcela(s))");
            lancamentoPagamentoParcela.setDataHora(LocalDateTime.now());
            caixaService.registrarLancamento(lancamentoPagamentoParcela);
            System.out.println("DEBUG: Saída de caixa de R$" + valorRealSaidaCaixa + " registrada para pagamento de parcela(s) do bem: " + bem.getNome());
        } catch (Exception e) {
            System.err.println("ERRO: Falha ao registrar pagamento de parcela no Caixa: " + e.getMessage());
            throw new RuntimeException("Erro ao registrar pagamento de parcela no Caixa.", e);
        }

        int novasParcelasPagas = bem.getParcelasPagas() + parcelasPagasNestaTransacao;
        bem.setParcelasPagas(Math.min(novasParcelasPagas, bem.getParcelasTotais()));

        bemRepository.save(bem);
    }
}