package com.loja.controller;

import com.loja.model.Cliente;
import com.loja.model.Produto;
import com.loja.model.Venda;
import com.loja.model.ItemVenda;
import com.loja.model.enums.FormaPagamento;
import com.loja.service.ClienteService;
import com.loja.service.ProdutoService;
import com.loja.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private VendaService vendaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vendas", vendaService.listarTodas());
        return "vendas/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute Venda venda, RedirectAttributes redirectAttributes) {
        try {
            vendaService.salvar(venda);
            redirectAttributes.addFlashAttribute("mensagem", "Venda registrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar venda: " + e.getMessage());
        }
        return "redirect:/vendas";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model) {
        model.addAttribute("venda", vendaService.buscarPorId(id));
        return "vendas/detalhes";
    }

    @GetMapping("/nova")
    public String novaVenda(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("formasPagamento", FormaPagamento.values());
        return "vendas/form";
    }

    @PostMapping("/salvar")
    public String salvarVenda(@RequestParam Long clienteId,
                             @RequestParam Long produtoId,
                             @RequestParam Integer quantidade,
                             @RequestParam FormaPagamento formaPagamento,
                             @RequestParam(required = false) Integer parcelas,
                             @RequestParam BigDecimal valorTotal,
                             RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.buscarPorId(clienteId);
            Produto produto = produtoService.buscarPorId(produtoId);

            Venda venda = new Venda();
            venda.setCliente(cliente);
            venda.setProduto(produto);
            venda.setQuantidade(quantidade);
            venda.setFormaPagamento(formaPagamento);
            venda.setParcelas(parcelas != null ? parcelas : 1);
            venda.setValorUnitario(produto.getPrecoVenda());
            venda.setValorTotal(valorTotal);

            vendaService.salvar(venda);
            redirectAttributes.addFlashAttribute("mensagem", "Venda registrada com sucesso!");
            return "redirect:/vendas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar venda: " + e.getMessage());
            return "redirect:/vendas/nova";
        }
    }

    @GetMapping("/{id}/deletar")
    public String deletarVenda(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vendaService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Venda excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir venda: " + e.getMessage());
        }
        return "redirect:/vendas";
    }
} 