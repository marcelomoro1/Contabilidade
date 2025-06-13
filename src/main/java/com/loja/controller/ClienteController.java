package com.loja.controller;

import com.loja.model.Cliente;
import com.loja.service.ClienteService;
import jakarta.validation.Valid; // Adicionar esta importação
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Adicionar esta importação
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("cliente", clienteService.buscarPorId(id));
            return "clientes/detalhes";
        } catch (Exception e) {
            logger.error("Erro ao buscar cliente: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado");
            return "redirect:/clientes";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("cliente", clienteService.buscarPorId(id));
            return "clientes/form";
        } catch (Exception e) {
            logger.error("Erro ao buscar cliente para edição: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado");
            return "redirect:/clientes";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Cliente cliente, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        // Validação usando Bean Validation
        if (result.hasErrors()) {
            logger.warn("Erros de validação ao salvar cliente: {}", result.getAllErrors());
            return "clientes/form"; // Retorna para o formulário com os erros
        }

        try {
            logger.info("Tentando salvar cliente: {}", cliente.getNome());

            // Remove caracteres especiais do CPF/CNPJ (se necessário, pode ser feito no Service)
            cliente.setCpf(cliente.getCpf().replaceAll("[^0-9]", ""));

            // Normaliza o estado para maiúsculas (se necessário, pode ser feito no Service)
            if (cliente.getEstado() != null) {
                cliente.setEstado(cliente.getEstado().toUpperCase());
            }

            logger.info("Dados do cliente validados, tentando salvar no banco de dados");
            Cliente clienteSalvo = clienteService.salvar(cliente);
            logger.info("Cliente salvo com sucesso. ID: {}", clienteSalvo.getId());

            redirectAttributes.addFlashAttribute("mensagem", "Cliente salvo com sucesso!");
            return "redirect:/clientes";
        } catch (Exception e) {
            logger.error("Erro ao salvar cliente: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar cliente: " + e.getMessage());
            // Se o erro não for de validação (ex: duplicidade de CPF no banco),
            // podemos redirecionar e manter os dados para o usuário tentar novamente.
            // Para isso, precisamos adicionar o objeto "cliente" como flash attribute.
            redirectAttributes.addFlashAttribute("cliente", cliente);
            // Redireciona para o formulário de novo cliente.
            return "redirect:/clientes/novo";
        }
    }

    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Tentando excluir cliente com ID: {}", id);
            clienteService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Cliente excluído com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao excluir cliente: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }
}