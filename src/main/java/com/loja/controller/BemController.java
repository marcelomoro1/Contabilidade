package com.loja.controller;

import com.loja.model.Bem;
import com.loja.model.enums.TipoBem;
import com.loja.service.BemService;
import jakarta.validation.Valid; // Adicionar esta importação
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Adicionar esta importação
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/bens")
public class BemController {

    @Autowired
    private BemService bemService;

    @GetMapping
    public String listarBens(Model model) {
        model.addAttribute("bens", bemService.listarTodos());
        return "bens/lista";
    }

    @GetMapping("/novo")
    public String novoBem(Model model) {
        model.addAttribute("bem", new Bem());
        model.addAttribute("tiposBem", TipoBem.values());
        return "bens/form";
    }

    @GetMapping("/{id}")
    public String detalharBem(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // Adicionando tratamento para bem não encontrado nos detalhes
        try {
            model.addAttribute("bem", bemService.buscarPorId(id));
            return "bens/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Bem não encontrado: " + e.getMessage());
            return "redirect:/bens";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarBem(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // Adicionando tratamento para bem não encontrado na edição
        try {
            model.addAttribute("bem", bemService.buscarPorId(id));
            model.addAttribute("tiposBem", TipoBem.values());
            return "bens/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Bem não encontrado para edição: " + e.getMessage());
            return "redirect:/bens";
        }
    }

    @PostMapping("/salvar")
    public String salvarBem(@Valid @ModelAttribute Bem bem, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tiposBem", TipoBem.values()); // Recarregar enums para o formulário
            return "bens/form"; // Retorna para o formulário com erros de validação
        }
        try {
            bemService.salvar(bem);
            redirectAttributes.addFlashAttribute("mensagem", "Bem salvo com sucesso!");
            return "redirect:/bens";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar bem: " + e.getMessage());
            return "redirect:/bens";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluirBem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bemService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Bem excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir bem: " + e.getMessage());
        }
        return "redirect:/bens";
    }

    @PostMapping("/{id}/registrar-pagamento")
    public String registrarPagamentoBem(@PathVariable Long id,
                                        @RequestParam BigDecimal valorPago,
                                        RedirectAttributes redirectAttributes) {
        try {
            bemService.registrarPagamentoParcela(id, valorPago);
            redirectAttributes.addFlashAttribute("mensagem", "Pagamento registrado para o bem!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar pagamento: " + e.getMessage());
        }
        return "redirect:/bens/" + id;
    }
}