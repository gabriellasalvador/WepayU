package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoEhHoristaException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public abstract class Empregado implements Serializable{
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

    public void setComissao(double comissao) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public void addVenda(ResultadoVenda venda) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public List<ResultadoVenda> getVendas() throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    private String idSindicato;
    private double taxaSindical;
    private double dividaSindical = 0;
    private List<TaxaServico> taxasServico = new ArrayList<>();

    public String getIdSindicato() { return idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }


    public double getDividaSindical() { return dividaSindical; }
    public void setDividaSindical(double dividaSindical) {
        this.dividaSindical = dividaSindical;
    }


    public void sindicalizar(String idSindicato, double taxaSindical) {
        this.sindicalizado = true;
        this.idSindicato = idSindicato;
        this.taxaSindical = taxaSindical;
    }

    public void dessindicalizar() {
        this.sindicalizado = false;
        this.idSindicato = null;
        this.taxaSindical = 0;
    }

    public void addTaxaServico(TaxaServico taxa) {
        taxasServico.add(taxa);
    }

    public List<TaxaServico> getTaxasServico() {
        return taxasServico;
    }

    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setSalario(double salario) { this.salario = salario; }

    private String metodoPagamento = "emMaos";
    private String banco;
    private String agencia;
    private String contaCorrente;

    public String getMetodoPagamento() { return metodoPagamento; }
    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }

    public void definirPagamentoEmMaos() {
        metodoPagamento = "emMaos";
        banco = null; agencia = null; contaCorrente = null;
    }

    public void definirPagamentoCorreios() {
        metodoPagamento = "correios";
        banco = null; agencia = null; contaCorrente = null;
    }

    public void definirPagamentoBanco(String banco, String agencia, String contaCorrente) {
        metodoPagamento = "banco";
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
    }

    public void copiarEstadoComumDe(Empregado outro) {
        this.sindicalizado = outro.sindicalizado;
        this.idSindicato = outro.idSindicato;
        this.taxaSindical = outro.taxaSindical;
        this.taxasServico = outro.taxasServico;
        this.metodoPagamento = outro.metodoPagamento;
        this.banco = outro.banco;
        this.agencia = outro.agencia;
        this.contaCorrente = outro.contaCorrente;
        this.dividaSindical = outro.dividaSindical;

    }




}