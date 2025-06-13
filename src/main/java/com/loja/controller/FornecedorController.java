package com.loja.controller;

import com.loja.model.Fornecedor;
import com.loja.service.FornecedorService;
import jakarta.validation.Valid; // Adicionar esta importação
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Adicionar esta importação
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        return "fornecedores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedores/form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("fornecedor", fornecedorService.buscarPorId(id));
            return "fornecedores/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Fornecedor não encontrado: " + e.getMessage());
            return "redirect:/fornecedores";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("fornecedor", fornecedorService.buscarPorId(id));
            return "fornecedores/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Fornecedor não encontrado para edição: " + e.getMessage());
            return "redirect:/fornecedores";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Fornecedor fornecedor, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            return "fornecedores/form"; // Retorna para o formulário com os erros de validação
        }
        try {
            fornecedorService.salvar(fornecedor);
            redirectAttributes.addFlashAttribute("mensagem", "Fornecedor salvo com sucesso!");
            return "redirect:/fornecedores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar fornecedor: " + e.getMessage());
            // Mantém o objeto no flash para repopular o formulário em caso de erro de negócio
            redirectAttributes.addFlashAttribute("fornecedor", fornecedor);
            return "redirect:/fornecedores/novo"; // Redireciona para o formulário de novo fornecedor
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fornecedorService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Fornecedor excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir fornecedor: " + e.getMessage());
        }
        return "redirect:/fornecedores";
    }
}