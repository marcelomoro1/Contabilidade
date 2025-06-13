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
import com.loja.service.BalancoPatrimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        // **IMPORTANTE:** O método calcularValorTotalEstoque() no ProdutoRepository atualmente não filtra por data.
        // Se você precisar de um valor de estoque histórico, precisaria ajustar a query no ProdutoRepository
        // para considerar a quantidade e o custo dos produtos ATÉ a data de referência. Isso é mais complexo
        // pois exigiria um histórico de estoque ou uma lógica mais avançada de movimentação.
        // Por enquanto, ele pegará o valor do estoque ATUAL.
        BigDecimal valorEstoque = produtoRepository.calcularValorTotalEstoque();
        balanco.getBensDireitos().add(new ItemBensDireitosDTO("Estoque de Produtos", valorEstoque));
        totalAtivosCalculado = totalAtivosCalculado.add(valorEstoque);

        // 3. Ativo Não Circulante - Bens Imobilizados
        // **IMPORTANTE:** Assim como o estoque, buscar bens por data é mais complexo.
        // Aqui estamos pegando TODOS os bens existentes. Se você precisar de bens adquiridos ATÉ a data,
        // precisaria de um método no BemRepository como findByDataAquisicaoLessThanEqual(LocalDate data).
        List<Bem> todosBens = bemRepository.findAll();
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


        // --- PASSIVOS (Obrigações) ---
        BigDecimal totalPassivosCalculado = BigDecimal.ZERO;

        // Exemplo de passivo fixo. **ATENÇÃO:** Se você quiser um balanço por data,
        // sua lógica de passivos também precisaria filtrar as obrigações que existiam na data.
        BigDecimal passivoExemplo = new BigDecimal("0.00");
        balanco.getObrigacoes().add(new ItemObrigacoesDTO("Contas a Pagar Fornecedores (Exemplo)", passivoExemplo));
        totalPassivosCalculado = totalPassivosCalculado.add(passivoExemplo);


        // --- PATRIMÔNIO LÍQUIDO ---
        BigDecimal patrimonioLiquidoCalculado = BigDecimal.ZERO;

        // Capital Social (considera o valor do capital social existente até a data, se houver histórico de alterações)
        // O findTopByOrderByDataAberturaDesc() já pega o último registrado, o que geralmente está bom para a maioria dos casos.
        Optional<CapitalSocial> capitalSocialOpt = capitalSocialRepository.findTopByOrderByDataAberturaDesc();
        BigDecimal capitalSocialValor = capitalSocialOpt
                .filter(cs -> cs.getDataAbertura() != null && !cs.getDataAbertura().isAfter(dataReferencia)) // Considera capital social até a data
                .map(CapitalSocial::getValorAbertura)
                .orElse(BigDecimal.ZERO);
        balanco.getPatrimonioLiquidoItems().add(new ItemBensDireitosDTO("Capital Social", capitalSocialValor));
        patrimonioLiquidoCalculado = patrimonioLiquidoCalculado.add(capitalSocialValor);

        // Lucros ou Prejuízos Acumulados
        BigDecimal lucrosPrejuizosAcumulados = totalAtivosCalculado.subtract(totalPassivosCalculado).subtract(capitalSocialValor);
        balanco.getPatrimonioLiquidoItems().add(new ItemBensDireitosDTO("Lucros/Prejuízos Acumulados", lucrosPrejuizosAcumulados));
        patrimonioLiquidoCalculado = patrimonioLiquidoCalculado.add(lucrosPrejuizosAcumulados);

        // Define os totais calculados no DTO
        balanco.setTotalAtivos(totalAtivosCalculado);
        balanco.setTotalPassivos(totalPassivosCalculado);
        balanco.setPatrimonioLiquido(patrimonioLiquidoCalculado);

        return balanco;
    }
}