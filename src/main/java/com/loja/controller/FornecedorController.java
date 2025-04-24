package com.loja.controller;

import com.loja.model.Fornecedor;
import com.loja.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.buscarPorId(id));
        return "fornecedores/detalhes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.buscarPorId(id));
        return "fornecedores/form";
    }

    @PostMapping("/salvar")
    public String salvar(Fornecedor fornecedor, RedirectAttributes redirectAttributes) {
        try {
            fornecedorService.salvar(fornecedor);
            redirectAttributes.addFlashAttribute("mensagem", "Fornecedor salvo com sucesso!");
            return "redirect:/fornecedores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar fornecedor: " + e.getMessage());
            return "redirect:/fornecedores/novo";
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