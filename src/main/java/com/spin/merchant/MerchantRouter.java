package com.spin.merchant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration(proxyBeanMethods = false)
public class MerchantRouter {
    @Bean
    public RouterFunction<ServerResponse> payInCallBack(MerchantHandler merchantHandler) {
        return RouterFunctions.route(POST("/payInCallBack").and(accept(MediaType.APPLICATION_FORM_URLENCODED)), merchantHandler::payInCallBack);
    }
    @Bean
    public RouterFunction<ServerResponse> payOutCallBack(MerchantHandler merchantHandler) {
        return RouterFunctions.route(POST("/payOutCallBack").and(accept(MediaType.APPLICATION_FORM_URLENCODED)), merchantHandler::payOutCallBack);
    }
    @Bean
    public RouterFunction<ServerResponse> payInWebhook(MerchantHandler merchantHandler) {
        return RouterFunctions.route(POST("/payInWebhook").and(accept(MediaType.APPLICATION_JSON)), merchantHandler::payInWebhook);
    }
    @Bean
    public RouterFunction<ServerResponse> payOutWebhook(MerchantHandler merchantHandler) {
        return RouterFunctions.route(POST("/payOutWebhook").and(accept(MediaType.APPLICATION_JSON)), merchantHandler::payOutWebhook);
    }
}
