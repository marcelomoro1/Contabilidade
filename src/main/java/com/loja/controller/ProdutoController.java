package com.loja.controller;

import com.loja.model.Produto;
import com.loja.service.ProdutoService; // Mudar para a interface se houver uma
import jakarta.validation.Valid; // Adicionar esta importação
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Adicionar esta importação
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService; // Usar a interface ProdutoService

    @GetMapping
    public String listarProdutos(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String novoProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "produtos/form";
    }

    @GetMapping("/{id}")
    public String detalharProduto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("produto", produtoService.buscarPorId(id));
            return "produtos/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado: " + e.getMessage());
            return "redirect:/produtos";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarProduto(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("produto", produtoService.buscarPorId(id));
            return "produtos/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Produto não encontrado para edição: " + e.getMessage());
            return "redirect:/produtos";
        }
    }

    @PostMapping("/salvar")
    public String salvarProduto(@Valid @ModelAttribute Produto produto, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            return "produtos/form";
        }
        try {
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("mensagem", "Produto salvo com sucesso!");
            return "redirect:/produtos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar produto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("produto", produto); // Mantém os dados no flash
            return "redirect:/produtos/novo";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluirProduto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Produto excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir produto: " + e.getMessage());
        }
        return "redirect:/produtos";
    }
}