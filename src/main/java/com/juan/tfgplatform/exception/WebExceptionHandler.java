package com.juan.tfgplatform.exception;

import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileTooLarge() {
        return "redirect:/alumno/ejercicios?errorArchivo=1";
    }
}
