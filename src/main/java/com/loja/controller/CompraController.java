package com.loja.controller;

import com.loja.model.Compra;
import com.loja.model.Fornecedor;
import com.loja.model.ItemCompra;
import com.loja.model.Produto;
import com.loja.model.enums.FormaPagamento; // Importe o enum do pacote correto
import com.loja.service.CompraService;
import com.loja.service.FornecedorService;
import com.loja.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.ArrayList; // Para a lista de itens, se necessário
import java.util.List; // Para a lista de itens, se necessário

@Controller
@RequestMapping("/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private FornecedorService fornecedorService;
    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public String listarCompras(Model model) {
        model.addAttribute("compras", compraService.listarTodas());
        return "compras/lista";
    }

    @GetMapping("/nova")
    public String novaCompra(Model model) {
        Compra compra = new Compra();
        if (compra.getItensCompra() == null) {
            compra.setItensCompra(new ArrayList<>());
        }
        ItemCompra item = new ItemCompra();
        // Preenche o crédito ICMS inicial (0)
        item.setCreditoIcms(BigDecimal.ZERO);
        compra.getItensCompra().add(item);
        model.addAttribute("compra", compra);
        model.addAttribute("fornecedores", fornecedorService.listarTodos());
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("formasPagamento", FormaPagamento.values());
        return "compras/form";
    }

    @PostMapping("/salvar")
    public String salvarCompra(@ModelAttribute("compra") Compra compra,
                               RedirectAttributes redirectAttributes) {
        try {

            // Garante que o fornecedor esteja vinculado à compra como uma entidade gerenciada
            if (compra.getFornecedor() != null && compra.getFornecedor().getId() != null) {
                Fornecedor fornecedor = fornecedorService.buscarPorId(compra.getFornecedor().getId());
                if (fornecedor != null) {
                    compra.setFornecedor(fornecedor);
                } else {
                    throw new IllegalArgumentException("Fornecedor selecionado não encontrado.");
                }
            }

            // Garante que os itens de compra estejam vinculados à compra e que os produtos sejam entidades gerenciadas
            if (compra.getItensCompra() != null && !compra.getItensCompra().isEmpty()) {
                for (ItemCompra item : compra.getItensCompra()) {
                    item.setCompra(compra); // Vincula o item à compra
                    if (item.getProduto() != null && item.getProduto().getId() != null) {
                        Produto produto = produtoService.buscarPorId(item.getProduto().getId());
                        if (produto != null) {
                            item.setProduto(produto); // Anexa o produto gerenciado
                        } else {
                            throw new IllegalArgumentException("Produto com ID " + item.getProduto().getId() + " não encontrado.");
                        }
                    } else {
                        // Tratar caso onde o produto não foi selecionado ou ID está ausente
                        throw new IllegalArgumentException("Produto não selecionado para um dos itens da compra.");
                    }
                }
            } else {
                throw new IllegalArgumentException("A compra deve ter pelo menos um item.");
            }

            compra.setDataCompra(LocalDateTime.now()); // Define a data da compra no momento do salvamento

            compraService.salvar(compra);
            redirectAttributes.addFlashAttribute("mensagem", "Compra registrada com sucesso!");
            return "redirect:/compras";
        } catch (IllegalArgumentException e) { // Captura exceções de validação de dados
            redirectAttributes.addFlashAttribute("erro", "Erro de validação: " + e.getMessage());
            return "redirect:/compras/nova"; // Redireciona para o formulário para correção
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro inesperado ao registrar compra: " + e.getMessage());
            return "redirect:/compras/nova";
        }
    }

    @GetMapping("/{id}")
    public String detalharCompra(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Compra compra = compraService.buscarPorId(id);
            if (compra != null) {
                // Garante que cada item tenha o crédito ICMS preenchido
                for (ItemCompra item : compra.getItensCompra()) {
                    if (item.getCreditoIcms() == null) {
                        BigDecimal credito = item.getPrecoUnitarioCompra() != null && item.getQuantidade() != null ?
                            item.getPrecoUnitarioCompra().multiply(BigDecimal.valueOf(item.getQuantidade())).multiply(BigDecimal.valueOf(0.17)) : BigDecimal.ZERO;
                        item.setCreditoIcms(credito);
                    }
                }
                model.addAttribute("compra", compra);
                return "compras/detalhes";
            } else {
                redirectAttributes.addFlashAttribute("erro", "Compra não encontrada.");
                return "redirect:/compras";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao buscar detalhes da compra: " + e.getMessage());
            return "redirect:/compras";
        }
    }

    @PostMapping("/{id}/registrar-pagamento")
    public String registrarPagamento(@PathVariable Long id,
                                     @RequestParam("valorPago") BigDecimal valorPago,
                                     RedirectAttributes ra) {
        try {
            compraService.registrarPagamento(id, valorPago);
            ra.addFlashAttribute("mensagem", "Pagamento registrado com sucesso!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erro", e.getMessage()); // Erro específico de validação
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao registrar pagamento: " + e.getMessage());
        }
        // Sempre redireciona para a página de detalhes da compra, independentemente do sucesso ou erro
        return "redirect:/compras/" + id;
    }


    @GetMapping("/excluir/{id}")
    public String excluirCompra(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            compraService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Compra excluída e estoque ajustado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir compra: " + e.getMessage());
        }
        return "redirect:/compras";
    }
}