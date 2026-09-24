package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.io.Serializable;

/**
 * Registro de um cartao de ponto: quantas horas um empregado horista trabalhou
 * em uma data especifica. Cada lancaCartao(...) na Facade cria um destes e
 * adiciona a lista de cartoes do empregado (EmpregadoHorista.cartoes).
 *
 * Implementa Serializable porque faz parte do estado do sistema salvo pela
 * Facade nas "fotos" usadas no undo/redo/persistencia.
 */
public class CartaoDePonto implements Serializable{
    private LocalDate data;
    private double horas;

    public CartaoDePonto(LocalDate data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    public LocalDate getData() {
        return data;
    }

    public double getHoras() {
        return horas;
    }


}