package com.juan.tfgplatform.exception;

import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.error.ErrorPageRegistrar;
import org.springframework.boot.web.error.ErrorPageRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CustomErrorController implements ErrorPageRegistrar {

    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        registry.addErrorPages(
            new ErrorPage(HttpStatus.PAYLOAD_TOO_LARGE, "/alumno/ejercicios?errorArchivo=1")
        );
    }
}
