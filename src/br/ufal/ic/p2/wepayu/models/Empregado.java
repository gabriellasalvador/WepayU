package br.ufal.ic.p2.wepayu.models;

public class Empregado {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private double salario;
    private double comissao;
    private boolean sindicalizado;

    public Empregado(String id, String nome, String endereco, String tipo, double salario){
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.sindicalizado = false;
    }

    public String getId(){
        return id;
    }
    public String getNome() {

        return nome;
    }

    public String getEndereco() {

        return endereco;
    }

    public String getTipo() {
        return tipo;
    }

    public double getSalario() {
        return salario;
    }

    public double getComissao() {
        return comissao;
    }

    public void setComissao(double comissao) {
        this.comissao = comissao;
    }

    public boolean isSindicalizado(){
        return sindicalizado;
    }
}
