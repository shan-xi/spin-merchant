package com.spin.merchant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;


@Controller
public class PayInController {
    private static final Logger log = LoggerFactory.getLogger(PayInController.class);
    private final WebClient webClient;

    @Autowired
    public PayInController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    @GetMapping("/pay-in-page")
    public Mono<String> payInPage(Model model) {
        model.addAttribute("message", "Merchant Hosted Pay In Page");
        return Mono.just("pay-in-page");
    }
}
