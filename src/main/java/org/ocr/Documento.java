package org.ocr;

import lombok.Data;

import java.io.Serializable;

@Data
public class Documento implements Serializable {
    private String conteudo;
    //private String autor;
    private String caminho;
    public Documento(){
    }
    Documento(String conteudo, String caminho){
        this.conteudo = conteudo;
        this.caminho = caminho;

    }
}