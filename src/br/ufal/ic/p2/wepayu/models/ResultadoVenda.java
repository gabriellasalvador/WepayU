package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class ResultadoVenda {
    private LocalDate data;
    private double valor;

    public ResultadoVenda(LocalDate data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() { return data; }
    public double getValor() { return valor; }
}