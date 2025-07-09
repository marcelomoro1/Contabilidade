package com.loja.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.loja.service.ClienteService;
import com.loja.service.ProdutoService;
import com.loja.service.VendaService;
import com.loja.service.CaixaService; // Importar CaixaService

import java.time.LocalDate;
import java.math.BigDecimal; // Importar BigDecimal para trabalhar com valores monetários

@Controller
public class HomeController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private CaixaService caixaService;

    @GetMapping("/")
    public String home(Model model) {
        LocalDate hoje = LocalDate.now();

        Long totalVendas = vendaService.countVendasByData(hoje);
        model.addAttribute("totalVendas", totalVendas);


        Double faturamentoDouble = vendaService.calcularFaturamentoPorData(hoje);
        BigDecimal faturamento = (faturamentoDouble != null) ? BigDecimal.valueOf(faturamentoDouble) : BigDecimal.ZERO;
        model.addAttribute("faturamento", faturamento);


        Long totalProdutos = produtoService.countProdutosEmEstoque();
        model.addAttribute("totalProdutos", totalProdutos);

        Long totalClientes = clienteService.countClientes();
        model.addAttribute("totalClientes", totalClientes);

        BigDecimal saldoCaixaAtual = caixaService.calcularSaldoAtual();

        if (saldoCaixaAtual == null) {
            saldoCaixaAtual = BigDecimal.ZERO;
        }
        model.addAttribute("saldoCaixaAtual", saldoCaixaAtual);


        return "index";
    }
}