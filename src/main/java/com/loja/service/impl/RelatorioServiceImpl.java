package com.loja.service.impl;

import com.loja.dto.BalancoPatrimonialDTO;
import com.loja.dto.ItemBensDireitosDTO;
import com.loja.dto.ItemObrigacoesDTO;
import com.loja.model.*; // Importa suas classes de modelo (Bem, CapitalSocial, Venda, Produto, Compra)
import com.loja.model.enums.FormaPagamento; // Importa sua enum FormaPagamento
import com.loja.repository.BemRepository;
import com.loja.repository.CapitalSocialRepository;
import com.loja.repository.CompraRepository; // Seu repositório de Compras
import com.loja.repository.VendaRepository;
import com.loja.repository.ProdutoRepository; // Seu repositório de Produtos
import com.loja.service.RelatorioService; // Importa a interface do serviço

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Necessário para Optional<CapitalSocial>
import java.util.Objects; // Para usar Objects.requireNonNullElse

@Service
public class RelatorioServiceImpl implements RelatorioService {

    @Autowired
    private BemRepository bemRepository;
    @Autowired
    private CapitalSocialRepository capitalSocialRepository;
    @Autowired
    private CompraRepository compraRepository; // Repositório para suas compras
    @Autowired
    private VendaRepository vendaRepository;
    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    @Transactional(readOnly = true)
    public BalancoPatrimonialDTO generarBalancoPatrimonial() {
        // Inicializa os totais
        BigDecimal totalAtivos = BigDecimal.ZERO;
        BigDecimal totalPassivos = BigDecimal.ZERO;

        // Inicializa as listas de itens detalhados para o DTO
        List<ItemBensDireitosDTO> bensDireitos = new ArrayList<>();
        List<ItemObrigacoesDTO> obrigacoes = new ArrayList<>();
        List<ItemBensDireitosDTO> patrimonioLiquidoItems = new ArrayList<>(); // Para detalhar o PL

        // --- 1. Cálculo de ATIVOS (Bens e Direitos) ---

        //Bens Adquiridos (Patrimônio Fixo)
        List<Bem> bens = bemRepository.findAll();
        BigDecimal valorTotalBens = bens.stream()
                .map(Bem::getValorAquisicao)
                .filter(Objects::nonNull) // Garante que o valor de aquisição não é nulo
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAtivos = totalAtivos.add(valorTotalBens);
        bensDireitos.add(new ItemBensDireitosDTO("Bens Adquiridos (Patrimônio Fixo)", valorTotalBens));

        //Mercadorias para Revenda (Estoque de Produtos)
        List<Produto> produtos = produtoRepository.findAll();
        BigDecimal valorEstoqueMercadorias = produtos.stream()
            .map(p -> {
                Integer quantidade = Objects.requireNonNullElse(p.getQuantidade(), 0);
                return BigDecimal.valueOf(quantidade);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAtivos = totalAtivos.add(valorEstoqueMercadorias);
        bensDireitos.add(new ItemBensDireitosDTO("Mercadorias para Revenda (Estoque)", valorEstoqueMercadorias));

        //ontas a Receber de Clientes (Vendas a Prazo)
        List<Venda> vendasAPrazo = vendaRepository.findByFormaPagamento(FormaPagamento.APRAZO);
        BigDecimal valorContasAReceber = vendasAPrazo.stream()
                .map(Venda::getSaldoAReceber)
                .filter(Objects::nonNull) // Garante que o saldo a receber não é nulo
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAtivos = totalAtivos.add(valorContasAReceber);
        bensDireitos.add(new ItemBensDireitosDTO("Contas a Receber de Clientes", valorContasAReceber));

        //Caixa/Banco (Estimativa simplificada)
        BigDecimal saldoCaixaBanco = BigDecimal.valueOf(25000.00);
        totalAtivos = totalAtivos.add(saldoCaixaBanco);
        bensDireitos.add(new ItemBensDireitosDTO("Caixa e Equivalentes de Caixa", saldoCaixaBanco));


        //Cálculo de PASSIVOS (Obrigações) ---

        //Contas a Pagar de Bens Adquiridos a Prazo
        BigDecimal valorContasPagarBens = bens.stream() // Reutiliza a lista de bens
                .filter(bem -> bem.getParcelasTotais() != null && bem.getParcelasPagas() != null && bem.getParcelasPagas() < bem.getParcelasTotais())
                .map(Bem::getSaldoDevedor)
                .filter(Objects::nonNull) // Garante que o saldo devedor não é nulo
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalPassivos = totalPassivos.add(valorContasPagarBens);
        obrigacoes.add(new ItemObrigacoesDTO("Contas a Pagar (Aquisição de Bens)", valorContasPagarBens));

        //Contas a Pagar de Mercadorias Adquiridas a Prazo
        List<Compra> comprasAPrazo = compraRepository.findByFormaPagamento(FormaPagamento.APRAZO);
        BigDecimal valorContasPagarMercadorias = comprasAPrazo.stream()
                .map(Compra::getSaldoDevedor)
                .filter(Objects::nonNull) //Garante que o saldo devedor não é nulo
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalPassivos = totalPassivos.add(valorContasPagarMercadorias);
        obrigacoes.add(new ItemObrigacoesDTO("Contas a Pagar (Aquisição de Mercadorias)", valorContasPagarMercadorias));


        //Cálculo do PATRIMÔNIO LÍQUIDO ---

        //Capital Social
        //Busca o capital social mais recente por data de abertura
        Optional<CapitalSocial> capitalSocialOpt = capitalSocialRepository.findTopByOrderByDataAberturaDesc();
        BigDecimal valorCapitalSocial = capitalSocialOpt.map(CapitalSocial::getValorAbertura).orElse(BigDecimal.ZERO);
        patrimonioLiquidoItems.add(new ItemBensDireitosDTO("Capital Social", valorCapitalSocial));

        //Lucro/Prejuízo Acumulado
        // A lógica de Lucro/Prejuízo é Ativo = Passivo + Patrimônio Líquido
        // Então, Lucro/Prejuízo Acumulado = Ativo Total - Passivo Total - Capital Social
        BigDecimal lucroPrejuizoAcumulado = totalAtivos.subtract(totalPassivos).subtract(valorCapitalSocial);
        patrimonioLiquidoItems.add(new ItemBensDireitosDTO("Lucros/Prejuízos Acumulados", lucroPrejuizoAcumulado));

        //Cálculo Final do Patrimônio Líquido
        // Se a equação contábil estiver balanceada, PL = Capital Social + Lucro/Prejuízo
        // Para garantir A = P + PL, calculamos PL como (Ativo - Passivo)
        BigDecimal patrimonioLiquidoCalculado = totalAtivos.subtract(totalPassivos);


        //Preencher e Retornar o DTO de Balanço Patrimonial
        BalancoPatrimonialDTO balanco = new BalancoPatrimonialDTO();
        balanco.setTotalAtivos(totalAtivos);
        balanco.setTotalPassivos(totalPassivos);
        balanco.setPatrimonioLiquido(patrimonioLiquidoCalculado); // Usa o valor calculado
        balanco.setBensDireitos(bensDireitos);
        balanco.setObrigacoes(obrigacoes);
        balanco.setPatrimonioLiquidoItems(patrimonioLiquidoItems);

        return balanco;
    }
}