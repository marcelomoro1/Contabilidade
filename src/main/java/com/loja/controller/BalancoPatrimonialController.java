package com.loja.controller;

import com.loja.dto.BalancoPatrimonialDTO;
import com.loja.service.BalancoPatrimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/balanco-patrimonial")
public class BalancoPatrimonialController {

    @Autowired
    private BalancoPatrimonialService balancoPatrimonialService;

    @GetMapping
    public String exibirBalanco(Model model,
                                @RequestParam(value = "data", required = false) String dataStr,
                                RedirectAttributes redirectAttributes) {
        try {
            LocalDate dataReferencia;
            if (dataStr != null && !dataStr.isEmpty()) {
                dataReferencia = LocalDate.parse(dataStr); // Assumindo formato YYYY-MM-DD
            } else {
                dataReferencia = LocalDate.now(); // Padrão para a data atual
            }

            BalancoPatrimonialDTO balanco = balancoPatrimonialService.gerarBalancoPorData(dataReferencia);
            model.addAttribute("balanco", balanco);
            model.addAttribute("dataReferencia", dataReferencia); // Para exibir no template

            return "balancoPatrimonial/detalhes"; // Retorna o template de detalhes do balanço
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("erro", "Formato de data inválido. Use AAAA-MM-DD.");
            return "redirect:/balanco-patrimonial"; // Redireciona para a página atual sem a data inválida
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao gerar Balanço Patrimonial: " + e.getMessage());
            e.printStackTrace(); // Para debug, em produção logue isso adequadamente
            return "redirect:/"; // Redireciona para a home ou uma página de erro geral
        }
    }
}