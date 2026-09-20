package br.ufal.ic.p2.wepayu.models;

public class EmpregadoAssalariado extends Empregado {

    public EmpregadoAssalariado(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario);
    }

    @Override
    public String getTipo() {
        return "assalariado";
    }
}