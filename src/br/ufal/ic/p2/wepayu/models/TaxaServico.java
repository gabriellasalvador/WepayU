package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.io.Serializable;

public class TaxaServico implements Serializable{
    private LocalDate data;
    private double valor;

    public TaxaServico(LocalDate data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() { return data; }
    public double getValor() { return valor; }
}
