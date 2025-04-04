package com.trabalho.apihamburgueria.controller;

import com.trabalho.apihamburgueria.model.Produto;
import com.trabalho.apihamburgueria.repository.ProdutoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/produto")
public class ProdutoController {


    private final ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping("/novo")
    public String formNovo(Model model) {
        model.addAttribute("produto", new Produto());
        return "produto";
    }

    @PostMapping
    public String salvar(@ModelAttribute Produto produto) {
        System.out.println(produto.toString());
        produtoRepository.save(produto);
        return "redirect:/produto";
    }

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        return "lista";
    }

    //Não deveria ser com get mas estamos fazendo de forma rapida
    @GetMapping("deletar/{id}")
    public String delete(@PathVariable Long id) {
        produtoRepository.deleteById(id);
        return "redirect:/produto";
    }

    //Get nao esta certo mas teria que ser feito de outra maneira
    @GetMapping("/editar/{id}")
    public String update(@PathVariable Long id, Model model) {
        Produto p = produtoRepository.findById(id).get();
        model.addAttribute("produto", p);
        return "produto";
    }



}
