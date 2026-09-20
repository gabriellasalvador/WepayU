package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends Empregado {
    private double comissao;
    private List<ResultadoVenda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco, double salario, double comissao) {
        super(id, nome, endereco, salario);
        this.comissao = comissao;
    }

    @Override
    public String getTipo() { return "comissionado"; }

    @Override
    public double getComissao() { return comissao; }

    @Override
    public void addVenda(ResultadoVenda venda) {
        vendas.add(venda);
    }

    @Override
    public List<ResultadoVenda> getVendas() {
        return vendas;
    }
}