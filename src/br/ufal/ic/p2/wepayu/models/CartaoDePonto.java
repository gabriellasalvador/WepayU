package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.io.Serializable;

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
