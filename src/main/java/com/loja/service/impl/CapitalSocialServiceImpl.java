package com.loja.service.impl;

import com.loja.model.CapitalSocial;
import com.loja.model.LancamentoCaixa; // Importar LancamentoCaixa
import com.loja.model.enums.TipoLancamentoCaixa; // Importar TipoLancamentoCaixa
import com.loja.repository.CapitalSocialRepository;
import com.loja.service.CapitalSocialService;
import com.loja.service.CaixaService; // <-- ESSENCIAL: Importar CaixaService AQUI!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // Importar BigDecimal
import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.List;
import java.util.Optional;

@Service
public class CapitalSocialServiceImpl implements CapitalSocialService {

    @Autowired
    private CapitalSocialRepository capitalSocialRepository;

    @Autowired
    private CaixaService caixaService; // <-- **PASSO 1: INJETAR O CAIXASERVICE**

    @Override
    @Transactional
    public CapitalSocial salvar(CapitalSocial capitalSocial) {
        // Primeiro, salva o registro do Capital Social em si.
        CapitalSocial capitalSalvo = capitalSocialRepository.save(capitalSocial);

        // --- **PASSO 2: CRIAR O LANÇAMENTO NO CAIXA** ---
        // Este bloco de código é a **parte que faltava** no seu CapitalSocialServiceImpl
        // e que vai resolver o problema do balanço.
        try {
            // Cria um novo lançamento de caixa do tipo ENTRADA
            LancamentoCaixa lancamentoCaixa = new LancamentoCaixa();
            lancamentoCaixa.setTipo(TipoLancamentoCaixa.ENTRADA);
            lancamentoCaixa.setValor(capitalSalvo.getValorAbertura()); // Pega o valor do Capital Social que acabou de ser salvo
            lancamentoCaixa.setDescricao("Integralização de Capital Social");
            lancamentoCaixa.setDataHora(LocalDateTime.now()); // Data e hora do lançamento

            // Salva o lançamento no caixa usando o CaixaService
            caixaService.registrarLancamento(lancamentoCaixa);
            System.out.println("DEBUG: Entrada de Capital Social de R$" + capitalSalvo.getValorAbertura() + " registrada no Caixa.");

        } catch (Exception e) {
            System.err.println("ERRO: Falha ao registrar a entrada do Capital Social no Caixa: " + e.getMessage());
            // É importante considerar como tratar este erro. Você pode querer lançar
            // uma exceção novamente para que a transação inteira (salvar capital e registrar caixa)
            // seja desfeita, garantindo a atomicidade.
            throw new RuntimeException("Erro ao processar o Capital Social e registrar entrada no Caixa.", e);
        }
        // --- FIM DO BLOCO DE LANÇAMENTO NO CAIXA ---

        return capitalSalvo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CapitalSocial> buscarUltimoCapitalSocial() {
        return capitalSocialRepository.findTopByOrderByDataAberturaDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapitalSocial> listarTodos() {
        return capitalSocialRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public CapitalSocial buscarPorId(Long id) {
        return capitalSocialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de Capital Social não encontrado."));
    }
}