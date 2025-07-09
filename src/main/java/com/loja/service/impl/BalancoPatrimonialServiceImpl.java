package com.loja.service.impl;

import com.loja.dto.BalancoPatrimonialDTO;
import com.loja.dto.ItemBensDireitosDTO;
import com.loja.dto.ItemObrigacoesDTO;
import com.loja.model.CapitalSocial;
import com.loja.model.Bem;
import com.loja.repository.BemRepository;
import com.loja.repository.CapitalSocialRepository;
import com.loja.repository.LancamentoCaixaRepository;
import com.loja.repository.ProdutoRepository;
import com.loja.repository.CompraRepository;
import com.loja.service.BalancoPatrimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;
import com.loja.model.Compra;
import com.loja.model.enums.FormaPagamento;

@Service
public class BalancoPatrimonialServiceImpl implements BalancoPatrimonialService {

    @Autowired
    private LancamentoCaixaRepository lancamentoCaixaRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private CapitalSocialRepository capitalSocialRepository;
    @Autowired
    private BemRepository bemRepository;
    @Autowired
    private CompraRepository compraRepository;

    @Override
    public BalancoPatrimonialDTO gerarBalanco() {
        // Por padrão, se este método for chamado, ele pode gerar o balanço para a data atual.
        return gerarBalancoPorData(LocalDate.now());
    }

    @Override
    public BalancoPatrimonialDTO gerarBalancoPorData(LocalDate dataReferencia) {
        BalancoPatrimonialDTO balanco = new BalancoPatrimonialDTO();

        // **Converter LocalDate para LocalDateTime para uso em repositórios que exigem:**
        // Para calcular saldo do caixa ATÉ o final do dia da data de referência
        LocalDateTime dataHoraReferenciaFinalDoDia = dataReferencia.atTime(23, 59, 59);

        // --- ATIVOS (Bens e Direitos) ---
        BigDecimal totalAtivosCalculado = BigDecimal.ZERO;

        // 1. Ativo Circulante - Caixa e Equivalentes (considerando saldo até a data)
        BigDecimal saldoCaixa = lancamentoCaixaRepository.calcularSaldoAteData(dataHoraReferenciaFinalDoDia);
        balanco.getBensDireitos().add(new ItemBensDireitosDTO("Caixa e Equivalentes", saldoCaixa));
        totalAtivosCalculado = totalAtivosCalculado.add(saldoCaixa);

        // 2. Ativo Circulante - Estoque de Produtos
        BigDecimal valorEstoque = produtoRepository.findAll().stream()
            .map(produto -> {
                Integer quantidade = produto.getQuantidade() != null ? produto.getQuantidade() : 0;
                if (quantidade == 0) return BigDecimal.ZERO;
                // Buscar o último preço de compra do produto
                BigDecimal ultimoPrecoCompra = compraRepository.findAll().stream()
                    .flatMap(compra -> compra.getItensCompra().stream())
                    .filter(item -> item.getProduto() != null && item.getProduto().getId().equals(produto.getId()))
                    .sorted((a, b) -> b.getId().compareTo(a.getId())) // Pega o mais recente
                    .map(item -> item.getPrecoUnitarioCompra() != null ? item.getPrecoUnitarioCompra() : BigDecimal.ZERO)
                    .findFirst().orElse(BigDecimal.ZERO);
                return ultimoPrecoCompra.multiply(BigDecimal.valueOf(quantidade));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        balanco.getBensDireitos().add(new ItemBensDireitosDTO("Estoque de Produtos", valorEstoque));
        totalAtivosCalculado = totalAtivosCalculado.add(valorEstoque);

        // 3. Ativo Não Circulante - Bens Imobilizados
        List<Bem> todosBens = bemRepository.findAll();
        
        // Debug: Mostrar todos os bens encontrados
        System.out.println("=== DEBUG BENS ===");
        System.out.println("Data de referência: " + dataReferencia);
        System.out.println("Total de bens encontrados: " + todosBens.size());
        todosBens.forEach(bem -> {
            System.out.println("Bem: " + bem.getNome() + 
                             " | Valor: " + bem.getValorAquisicao() + 
                             " | Data Aquisição: " + bem.getDataAquisicao() +
                             " | Incluído no balanço: " + (bem.getDataAquisicao() != null && !bem.getDataAquisicao().isAfter(dataReferencia)));
        });
        
        List<ItemBensDireitosDTO> bensImobilizadosDetalhados = todosBens.stream()
                .filter(bem -> bem.getDataAquisicao() != null && !bem.getDataAquisicao().isAfter(dataReferencia)) // Filtra bens adquiridos até a data
                .map(bem -> new ItemBensDireitosDTO(bem.getNome(), bem.getDescricao(), bem.getValorAquisicao()))
                .collect(Collectors.toList());
        balanco.getBensDireitos().addAll(bensImobilizadosDetalhados);

        // Soma dos bens filtrados
        BigDecimal valorTotalBensImobilizados = bensImobilizadosDetalhados.stream()
                .map(ItemBensDireitosDTO::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAtivosCalculado = totalAtivosCalculado.add(valorTotalBensImobilizados);
        
        System.out.println("Bens incluídos no balanço: " + bensImobilizadosDetalhados.size());
        System.out.println("Valor total dos bens: " + valorTotalBensImobilizados);
        System.out.println("===================");

        // --- PASSIVOS (Obrigações) ---
        BigDecimal totalPassivosCalculado = BigDecimal.ZERO;

        // Contas a Pagar Fornecedores (Compras a Prazo)
        List<Compra> comprasAPrazo = compraRepository.findByFormaPagamento(FormaPagamento.APRAZO);
        BigDecimal contasPagarFornecedores = comprasAPrazo.stream()
                .map(Compra::getSaldoDevedor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (contasPagarFornecedores.compareTo(BigDecimal.ZERO) > 0) {
            balanco.getObrigacoes().add(new ItemObrigacoesDTO("Contas a Pagar Fornecedores", contasPagarFornecedores));
            totalPassivosCalculado = totalPassivosCalculado.add(contasPagarFornecedores);
        }

        // Contas a Pagar Bens (Bens adquiridos a prazo)
        List<Bem> bensAPrazo = bemRepository.findAll().stream()
                .filter(bem -> bem.getParcelasTotais() != null && bem.getParcelasPagas() != null && 
                              bem.getParcelasPagas() < bem.getParcelasTotais())
                .collect(Collectors.toList());
        
        BigDecimal contasPagarBens = bensAPrazo.stream()
                .map(Bem::getSaldoDevedor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (contasPagarBens.compareTo(BigDecimal.ZERO) > 0) {
            balanco.getObrigacoes().add(new ItemObrigacoesDTO("Contas a Pagar Bens", contasPagarBens));
            totalPassivosCalculado = totalPassivosCalculado.add(contasPagarBens);
        }

        // --- PATRIMÔNIO LÍQUIDO ---
        BigDecimal patrimonioLiquidoCalculado = BigDecimal.ZERO;

        // Capital Social - SEMPRE o valor original registrado, independente das movimentações
        Optional<CapitalSocial> capitalSocialOpt = capitalSocialRepository.findTopByOrderByDataAberturaDesc();
        BigDecimal capitalSocialValor = capitalSocialOpt
                .map(CapitalSocial::getValorAbertura)
                .orElse(BigDecimal.ZERO);
        balanco.getPatrimonioLiquidoItems().add(new ItemBensDireitosDTO("Capital Social", capitalSocialValor));
        patrimonioLiquidoCalculado = patrimonioLiquidoCalculado.add(capitalSocialValor);

        // Lucros ou Prejuízos Acumulados = Ativos - Passivos - Capital Social
        BigDecimal lucrosPrejuizosAcumulados = totalAtivosCalculado.subtract(totalPassivosCalculado).subtract(capitalSocialValor);
        balanco.getPatrimonioLiquidoItems().add(new ItemBensDireitosDTO("Lucros/Prejuízos Acumulados", lucrosPrejuizosAcumulados));
        patrimonioLiquidoCalculado = patrimonioLiquidoCalculado.add(lucrosPrejuizosAcumulados);

        // Logs de debug para verificar os cálculos
        System.out.println("=== DEBUG BALANÇO PATRIMONIAL ===");
        System.out.println("Total Ativos: " + totalAtivosCalculado);
        System.out.println("Total Passivos: " + totalPassivosCalculado);
        System.out.println("Capital Social: " + capitalSocialValor);
        System.out.println("Lucros/Prejuízos Acumulados: " + lucrosPrejuizosAcumulados);
        System.out.println("Patrimônio Líquido: " + patrimonioLiquidoCalculado);
        System.out.println("================================");

        // Define os totais calculados no DTO
        balanco.setTotalAtivos(totalAtivosCalculado);
        balanco.setTotalPassivos(totalPassivosCalculado);
        balanco.setPatrimonioLiquido(patrimonioLiquidoCalculado);

        return balanco;
    }
}