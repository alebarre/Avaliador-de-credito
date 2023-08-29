package io.github.cursodsouza.msavaliadorcredito.application.ex;

import lombok.Getter;

public class ErroComunicacoIcroservicesException extends Exception{

    @Getter
    private Integer status;
    
    public ErroComunicacoIcroservicesException(String mensagem, int status) {
        super(mensagem);
        this.status = status;
    }
}
