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
            model.addAttribute("venda", venda);
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
        // A lista de itensVenda já é inicializada no construtor da Venda como um ArrayList vazio.
        // Se desejar que o formulário já comece com uma linha de item vazia, descomente abaixo:
        // venda.addItemVenda(new ItemVenda()); // Adiciona um item vazio para começar
        model.addAttribute("venda", venda);
        return "vendas/form";
    }

    @PostMapping("/salvar")
    public String salvarVenda(@ModelAttribute("venda") Venda venda,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            // 1. Validação e Set Cliente
            if (venda.getCliente() == null || venda.getCliente().getId() == null) {
                throw new IllegalArgumentException("Cliente deve ser selecionado.");
            }
            Cliente cliente = clienteService.buscarPorId(venda.getCliente().getId());
            if (cliente == null) {
                throw new IllegalArgumentException("Cliente não encontrado.");
            }
            venda.setCliente(cliente); // Seta o objeto Cliente gerenciado

            // 2. Validação e Processamento dos Itens de Venda
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
                item.setProduto(produto); // Seta o objeto Produto gerenciado
                item.setVenda(venda); // Seta a referência bidirecional para a venda
                // Garante que precoUnitario é o preço de venda atual do produto,
                // e que quantidade não é nula.
                item.setPrecoUnitario(Objects.requireNonNullElse(produto.getPrecoVenda(), BigDecimal.ZERO));
                item.setQuantidade(Objects.requireNonNullElse(item.getQuantidade(), 0));

                totalVendaCalculado = totalVendaCalculado.add(item.getSubtotal()); // Usa o getSubtotal do ItemVenda
            }

            venda.setValorTotal(totalVendaCalculado.setScale(2, RoundingMode.HALF_UP)); // Define o valor total da venda no backend

            // 3. Define a Data da Venda
            venda.setDataVenda(LocalDateTime.now());

            // 4. Validação e Configuração da Forma de Pagamento e Parcelamento
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


            // 5. Salvar a Venda (que agora contém todos os dados e itens populados)
            vendaService.salvar(venda);
            redirectAttributes.addFlashAttribute("mensagem", "Venda registrada com sucesso!");
            return "redirect:/vendas";

        } catch (Exception e) {
            // Logar o erro para depuração
            System.err.println("Erro ao registrar venda: " + e.getMessage());
            e.printStackTrace(); // Em produção, use um logger (ex: SLF4J)

            // Adiciona a mensagem de erro para ser exibida no formulário
            model.addAttribute("erro", "Erro ao registrar venda: " + e.getMessage());

            // Recarrega os dados auxiliares para o formulário
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("produtos", produtoService.listarTodos());
            model.addAttribute("formasPagamento", FormaPagamento.values());

            // O objeto 'venda' que veio do formulário (e foi parcialmente preenchido/validado)
            // é passado de volta para manter os dados preenchidos pelo usuário.
            // Se o cliente ou produtos não foram encontrados, eles serão nulos no 'venda' retornado.
            model.addAttribute("venda", venda);

            return "vendas/form"; // Retorna para o formulário, sem redirecionar
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