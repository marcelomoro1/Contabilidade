package com.loja.controller;

import com.loja.model.CapitalSocial;
import com.loja.service.CapitalSocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/capital-social")
public class CapitalSocialController {

    @Autowired
    private CapitalSocialService capitalSocialService;

    @GetMapping
    public String exibirCapitalSocial(Model model, RedirectAttributes redirectAttributes) { // Adicionado RedirectAttributes
        try {
            // Se buscarUltimoCapitalSocial() pode retornar vazio e você quer uma mensagem
            Optional<CapitalSocial> ultimoCapital = capitalSocialService.buscarUltimoCapitalSocial();
            if (ultimoCapital.isPresent()) {
                model.addAttribute("capitalSocial", ultimoCapital.get());
            } else {
                model.addAttribute("capitalSocial", new CapitalSocial()); // Ou deixe vazio para o template lidar
                model.addAttribute("info", "Nenhum registro de Capital Social encontrado. Comece adicionando um novo.");
            }
            model.addAttribute("historicoCapital", capitalSocialService.listarTodos());
            return "capitalSocial/detalhes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao carregar Capital Social: " + e.getMessage());
            return "redirect:/"; // Redireciona para a home ou outra página em caso de erro grave
        }
    }

    @GetMapping("/novo")
    public String novoCapitalSocial(Model model) {
        model.addAttribute("capitalSocial", new CapitalSocial());
        return "capitalSocial/form";
    }

    @PostMapping("/salvar")
    public String salvarCapitalSocial(@ModelAttribute CapitalSocial capitalSocial, RedirectAttributes redirectAttributes) {
        try {
            capitalSocialService.salvar(capitalSocial);
            redirectAttributes.addFlashAttribute("mensagem", "Capital Social registrado com sucesso!");
            return "redirect:/capital-social";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar Capital Social: " + e.getMessage());
            // Se houver erros de validação no CapitalSocial (com @Valid), você retornaria para "capitalSocial/form"
            redirectAttributes.addFlashAttribute("capitalSocial", capitalSocial); // Mantém os dados no formulário
            return "redirect:/capital-social/novo"; // Redireciona para o formulário (mas perde dados flash)
            // Alternativa melhor para erro de validação: return "capitalSocial/form"; com @Valid e BindingResult
        }
    }

    // Opcional: Adicionar endpoints de edição e exclusão se necessário
    /*
    @GetMapping("/editar/{id}")
    public String editarCapitalSocial(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("capitalSocial", capitalSocialService.buscarPorId(id));
            return "capitalSocial/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Registro de Capital Social não encontrado para edição: " + e.getMessage());
            return "redirect:/capital-social";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluirCapitalSocial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            capitalSocialService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Registro de Capital Social excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir registro de Capital Social: " + e.getMessage());
        }
        return "redirect:/capital-social";
    }
    */
}