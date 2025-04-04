package com.trabalho.apihamburgueria.controller;

import ch.qos.logback.core.model.Model;
import com.trabalho.apihamburgueria.repository.ProdutoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    private final ProdutoRepository produtoRepository;

    public HomeController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    public String home(Model model) {
        return "home";
    }
}
