package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoEhHoristaException;

import java.util.List;

public abstract class Empregado {
    private String id;
    private String nome;
    private String endereco;
    private double salario;
    private boolean sindicalizado;

    public Empregado(String id, String nome, String endereco, double salario) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.salario = salario;
        this.sindicalizado = false;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public double getSalario() { return salario; }
    public boolean isSindicalizado() { return sindicalizado; }

    public abstract String getTipo();

    public void addCartao(CartaoDePonto cartao) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    public List<CartaoDePonto> getCartoes() throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    public double getComissao() throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }
}