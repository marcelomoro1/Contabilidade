package com.loja.controller;

import com.loja.model.LancamentoCaixa;
import com.loja.model.enums.TipoLancamentoCaixa;
import com.loja.service.CaixaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/caixa")
public class CaixaController {

    @Autowired
    private CaixaService caixaService;

    @GetMapping
    public String exibirCaixa(Model model,
                              @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                              @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1); // Início do mês atual
        }
        if (dataFim == null) {
            dataFim = LocalDate.now(); // Data atual
        }

        BigDecimal saldoAtual = caixaService.calcularSaldoAtual();
        BigDecimal saldoNoFimDoPeriodo = caixaService.calcularSaldoPorData(dataFim);

        List<LancamentoCaixa> lancamentos = caixaService.listarLancamentosPorPeriodo(dataInicio, dataFim);

        model.addAttribute("saldoAtual", saldoAtual);
        model.addAttribute("saldoNoFimDoPeriodo", saldoNoFimDoPeriodo);
        model.addAttribute("lancamentos", lancamentos);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("novoLancamento", new LancamentoCaixa()); // Para o formulário de novo lançamento
        model.addAttribute("tiposLancamento", TipoLancamentoCaixa.values()); // Para o select do tipo

        return "caixa/detalhes";
    }

    @PostMapping("/registrar")
    public String registrarLancamento(@ModelAttribute("novoLancamento") LancamentoCaixa lancamento,
                                      RedirectAttributes redirectAttributes) {
        try {
            caixaService.registrarLancamento(lancamento);
            redirectAttributes.addFlashAttribute("mensagem", "Lançamento de caixa registrado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar lançamento: " + e.getMessage());
            redirectAttributes.addFlashAttribute("novoLancamento", lancamento); // Retorna os dados para o form
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro inesperado ao registrar lançamento.");
            redirectAttributes.addFlashAttribute("novoLancamento", lancamento);
        }
        return "redirect:/caixa";
    }
}