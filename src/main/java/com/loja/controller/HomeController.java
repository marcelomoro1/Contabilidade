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
    private CaixaService caixaService; // <-- NOVO: Injeção do CaixaService

    @GetMapping("/")
    public String home(Model model) {
        LocalDate hoje = LocalDate.now();

        Long totalVendas = vendaService.countVendasByData(hoje);
        model.addAttribute("totalVendas", totalVendas);

        // É uma boa prática usar BigDecimal para valores monetários para evitar problemas de ponto flutuante.
        Double faturamentoDouble = vendaService.calcularFaturamentoPorData(hoje);
        BigDecimal faturamento = (faturamentoDouble != null) ? BigDecimal.valueOf(faturamentoDouble) : BigDecimal.ZERO;
        model.addAttribute("faturamento", faturamento);


        Long totalProdutos = produtoService.countProdutosEmEstoque();
        model.addAttribute("totalProdutos", totalProdutos);

        Long totalClientes = clienteService.countClientes();
        model.addAttribute("totalClientes", totalClientes);

        // <-- NOVO: Obter e adicionar o saldo atual do caixa ao Model
        BigDecimal saldoCaixaAtual = caixaService.calcularSaldoAtual();
        // Garante que, se o serviço retornar null (improvável com COALESCE no SQL, mas por segurança),
        // o saldo seja BigDecimal.ZERO
        if (saldoCaixaAtual == null) {
            saldoCaixaAtual = BigDecimal.ZERO;
        }
        model.addAttribute("saldoCaixaAtual", saldoCaixaAtual);
        // --> FIM NOVO

        return "index"; // Assumindo que você tem um 'index.html' Thymeleaf template
    }
}