package br.ufal.ic.p2.wepayu.models;

public class EmpregadoComissionado extends Empregado {
    private double comissao;

    public EmpregadoComissionado(String id, String nome, String endereco, double salario, double comissao) {
        super(id, nome, endereco, salario);
        this.comissao = comissao;
    }

    @Override
    public String getTipo() { return "comissionado"; }

    @Override
    public double getComissao() { return comissao; }
}