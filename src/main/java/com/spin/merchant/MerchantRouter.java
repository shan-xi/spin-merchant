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
    public RouterFunction<ServerResponse> merchantRoute(MerchantHandler merchantHandler) {

        return RouterFunctions
                .route(POST("/payInCallBack")
                        .and(accept(MediaType.APPLICATION_FORM_URLENCODED)), merchantHandler::payInCallBack);
    }
}
