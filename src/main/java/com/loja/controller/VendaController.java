package com.loja.controller;

import com.loja.model.Cliente;
import com.loja.model.Produto;
import com.loja.model.Venda;
import com.loja.model.ItemVenda;
import com.loja.model.enums.FormaPagamento;
import com.loja.service.ClienteService;
import com.loja.service.ProdutoService;
import com.loja.service.VendaService;
import com.loja.repository.CompraRepository;
import com.loja.model.ItemCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode; // Importar para arredondamento
import java.time.LocalDateTime;
import java.util.Objects; // Importar Objects

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private CompraRepository compraRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vendas", vendaService.listarTodas());
        return "vendas/lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Venda venda = vendaService.buscarPorId(id);
            if (venda == null) {
                throw new IllegalArgumentException("Venda não encontrada com ID: " + id);
            }
            // Garante que cada item tenha débito e crédito ICMS preenchidos
            for (ItemVenda item : venda.getItensVenda()) {
                if (item.getDebitoIcms() == null && item.getPrecoUnitario() != null && item.getQuantidade() != null) {
                    item.setDebitoIcms(item.getPrecoUnitario().multiply(BigDecimal.valueOf(0.17)).multiply(BigDecimal.valueOf(item.getQuantidade())));
                }
                if (item.getCreditoIcms() == null && item.getProduto() != null && item.getProduto().getId() != null) {
                    BigDecimal creditoIcms = compraRepository.findAll().stream()
                        .flatMap(compra -> compra.getItensCompra().stream())
                        .filter(ic -> ic.getProduto().getId().equals(item.getProduto().getId()))
                        .sorted((a, b) -> b.getId().compareTo(a.getId()))
                        .map(ItemCompra::getCreditoIcms)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
                    item.setCreditoIcms(creditoIcms);
                }
            }
            // Calcular valor total de compra dos produtos vendidos
            BigDecimal valorTotalCompra = venda.getItensVenda().stream()
                .map(item -> {
                    if (item.getProduto() != null && item.getProduto().getId() != null) {
                        BigDecimal precoCompra = compraRepository.findAll().stream()
                            .flatMap(compra -> compra.getItensCompra().stream())
                            .filter(ic -> ic.getProduto().getId().equals(item.getProduto().getId()))
                            .sorted((a, b) -> b.getId().compareTo(a.getId()))
                            .map(ItemCompra::getPrecoUnitarioCompra)
                            .findFirst()
                            .orElse(BigDecimal.ZERO);
                        return precoCompra.multiply(BigDecimal.valueOf(item.getQuantidade()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal icmsDebito = venda.getValorTotal().multiply(BigDecimal.valueOf(0.17));
            BigDecimal icmsCredito = valorTotalCompra.multiply(BigDecimal.valueOf(0.17));
            BigDecimal icmsAPagar = icmsDebito.subtract(icmsCredito);
            model.addAttribute("venda", venda);
            model.addAttribute("valorTotalCompra", valorTotalCompra);
            model.addAttribute("icmsAPagar", icmsAPagar);
            return "vendas/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao buscar venda: " + e.getMessage());
            return "redirect:/vendas";
        }
    }

    @GetMapping("/nova")
    public String novaVenda(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("formasPagamento", FormaPagamento.values());
        Venda venda = new Venda();
        // Inicializa um item vazio sem valores pré-definidos
        ItemVenda item = new ItemVenda();
        venda.addItemVenda(item);
        model.addAttribute("venda", venda);
        return "vendas/form";
    }

    @PostMapping("/salvar")
    public String salvarVenda(@ModelAttribute("venda") Venda venda,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            //Validação e Set Cliente
            if (venda.getCliente() == null || venda.getCliente().getId() == null) {
                throw new IllegalArgumentException("Cliente deve ser selecionado.");
            }
            Cliente cliente = clienteService.buscarPorId(venda.getCliente().getId());
            if (cliente == null) {
                throw new IllegalArgumentException("Cliente não encontrado.");
            }
            venda.setCliente(cliente); // Seta o objeto Cliente gerenciado

            //Validação e Processamento dos Itens de Venda
            // Remove itens vazios ou inválidos que podem vir do formulário (sem produto ou quantidade)
            if (venda.getItensVenda() != null) { // Adiciona verificação de null
                venda.getItensVenda().removeIf(item ->
                        item == null ||
                                item.getProduto() == null ||
                                item.getProduto().getId() == null ||
                                item.getQuantidade() == null ||
                                item.getQuantidade() <= 0
                );
            }

            if (venda.getItensVenda() == null || venda.getItensVenda().isEmpty()) {
                throw new IllegalArgumentException("A venda deve conter pelo menos um item válido (produto e quantidade).");
            }

            BigDecimal totalVendaCalculado = BigDecimal.ZERO;
            for (ItemVenda item : venda.getItensVenda()) {
                Produto produto = produtoService.buscarPorId(item.getProduto().getId());
                if (produto == null) {
                    throw new IllegalArgumentException("Produto não encontrado para o item: " + item.getProduto().getId());
                }
                item.setProduto(produto);
                item.setVenda(venda);
                item.setPrecoUnitario(Objects.requireNonNullElse(item.getPrecoUnitario(), BigDecimal.ZERO));
                item.setQuantidade(Objects.requireNonNullElse(item.getQuantidade(), 0));
                BigDecimal debitoIcms = item.getPrecoUnitario().multiply(BigDecimal.valueOf(0.17)).multiply(BigDecimal.valueOf(item.getQuantidade()));
                item.setDebitoIcms(debitoIcms);
                // Buscar o último crédito ICMS do produto nas compras
                BigDecimal creditoIcms = compraRepository.findAll().stream()
                    .flatMap(compra -> compra.getItensCompra().stream())
                    .filter(ic -> ic.getProduto().getId().equals(produto.getId()))
                    .sorted((a, b) -> b.getId().compareTo(a.getId())) // Pega o mais recente
                    .map(ItemCompra::getCreditoIcms)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
                item.setCreditoIcms(creditoIcms);
                totalVendaCalculado = totalVendaCalculado.add(item.getSubtotal()); // Usa o getSubtotal do ItemVenda
            }

            venda.setValorTotal(totalVendaCalculado.setScale(2, RoundingMode.HALF_UP)); // Define o valor total da venda no backend

            //Define a Data da Venda
            venda.setDataVenda(LocalDateTime.now());

            //Validação e Configuração da Forma de Pagamento e Parcelamento
            if (venda.getFormaPagamento() == null) {
                throw new IllegalArgumentException("Forma de pagamento deve ser selecionada.");
            }

            if (venda.getFormaPagamento() == FormaPagamento.APRAZO) {
                if (venda.getParcelas() == null || venda.getParcelas() <= 0) {
                    throw new IllegalArgumentException("Número de parcelas inválido para pagamento a prazo.");
                }
                // Calcular valor da parcela
                BigDecimal valorParcelaCalculado = totalVendaCalculado.divide(BigDecimal.valueOf(venda.getParcelas()), 2, RoundingMode.HALF_UP);
                venda.setValorParcela(valorParcelaCalculado);
                venda.setSaldoAReceber(totalVendaCalculado); // Inicialmente, o saldo a receber é o total da venda
                venda.setParcelasPagas(0); // Começa com 0 parcelas pagas
            } else {
                venda.setParcelas(1); // Para pagamentos à vista, sempre 1 parcela
                venda.setValorParcela(totalVendaCalculado.setScale(2, RoundingMode.HALF_UP)); // O valor da parcela é o total
                venda.setSaldoAReceber(BigDecimal.ZERO); // Se for à vista, não há saldo a receber
                venda.setParcelasPagas(1); // Se à vista, considera-se 1 parcela paga imediatamente
            }


            //Salvar a Venda
            vendaService.salvar(venda);
            redirectAttributes.addFlashAttribute("mensagem", "Venda registrada com sucesso!");
            return "redirect:/vendas";

        } catch (Exception e) {
            //Logar o erro para depuração
            System.err.println("Erro ao registrar venda: " + e.getMessage());
            e.printStackTrace(); // Em produção, use um logger (ex: SLF4J)

            // Adiciona a mensagem de erro para ser exibida no formulário
            model.addAttribute("erro", "Erro ao registrar venda: " + e.getMessage());

            // Recarrega os dados auxiliares para o formulário
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("produtos", produtoService.listarTodos());
            model.addAttribute("formasPagamento", FormaPagamento.values());
            model.addAttribute("venda", venda);

            return "vendas/form";
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