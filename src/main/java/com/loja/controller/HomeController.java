package com.loja.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.loja.service.ClienteService;
import com.loja.service.ProdutoService;
import com.loja.service.VendaService;

import java.time.LocalDate;

@Controller
public class HomeController {
    
    @Autowired
    private VendaService vendaService;
    
    @Autowired
    private ProdutoService produtoService;
    
    @Autowired
    private ClienteService clienteService;
    
    @GetMapping("/")
    public String home(Model model) {
        // Obter dados para o dashboard
        LocalDate hoje = LocalDate.now();
        
        // Total de vendas hoje
        Long totalVendas = vendaService.countVendasByData(hoje);
        model.addAttribute("totalVendas", totalVendas);
        
        // Faturamento hoje
        Double faturamento = vendaService.calcularFaturamentoPorData(hoje);
        model.addAttribute("faturamento", faturamento);
        
        // Total de produtos em estoque
        Long totalProdutos = produtoService.countProdutosEmEstoque();
        model.addAttribute("totalProdutos", totalProdutos);
        
        // Total de clientes cadastrados
        Long totalClientes = clienteService.countClientes();
        model.addAttribute("totalClientes", totalClientes);
        
        return "index";
    }
} 