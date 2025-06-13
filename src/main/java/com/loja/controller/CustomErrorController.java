package com.loja.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.env.Environment; // Para verificar o perfil
import org.springframework.http.HttpStatus; // Para usar códigos HTTP
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    private final Environment environment; // Injetar Environment para verificar perfis

    // Injeção via construtor é preferível
    public CustomErrorController(Environment environment) {
        this.environment = environment;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Obtém o código de status HTTP do erro
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode = 500; // Default para Internal Server Error

        if (status instanceof Integer) {
            statusCode = (Integer) status;
        }

        // Obtém a mensagem de erro e a exceção original
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        // Registra o erro no log para depuração
        logger.error("Erro na aplicação: Status={}, Mensagem={}, Exceção={}", statusCode, errorMessage, exception != null ? exception.getMessage() : "N/A", exception);

        // Adiciona o código de status ao modelo
        model.addAttribute("status", statusCode);

        // Define uma mensagem amigável com base no status ou na existência da exceção
        String userFriendlyMessage;
        String technicalDetails = ""; // Detalhes técnicos, exibidos condicionalmente

        if (statusCode == HttpStatus.NOT_FOUND.value()) { // Erro 404
            userFriendlyMessage = "A página que você está procurando não foi encontrada.";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) { // Erro 403
            userFriendlyMessage = "Você não tem permissão para acessar este recurso.";
        } else if (exception != null) {
            userFriendlyMessage = "Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.";
            // Para ambientes de desenvolvimento, inclua detalhes técnicos
            if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev", "local"))) {
                technicalDetails = "Detalhes Técnicos: " + exception.getMessage();
                // O stack trace pode ser acessado diretamente no Thymeleaf via 'exception'
                // ou pode ser adicionado aqui para controle mais fino
                model.addAttribute("exceptionStackTrace", getStackTrace(exception));
            }
        } else {
            userFriendlyMessage = "Ocorreu um erro desconhecido. Por favor, tente novamente mais tarde.";
        }

        model.addAttribute("message", userFriendlyMessage);
        model.addAttribute("technicalDetails", technicalDetails); // Passa os detalhes técnicos

        // Passa a exceção original para o template, para que a stack trace possa ser exibida condicionalmente
        model.addAttribute("exception", exception);

        // Determina qual template de erro usar.
        // Você pode ter templates específicos para 404 (error-404.html) ou 500 (error-500.html)
        // Se não encontrar um específico, usa o genérico "error.html".
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return "error/404"; // Procura por src/main/resources/templates/error/404.html
        }
        // Para outros erros, pode-se criar mais ifs ou usar um template padrão
        return "error/error"; // Procura por src/main/resources/templates/error/error.html
    }

    /**
     * Helper method to convert a Throwable's stack trace to a String.
     * Useful for displaying in development mode.
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\t at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}