package br.ufal.ic.p2.wepayu.models.pagamento;

/**
 * Forma de pagamento "banco": o valor e depositado em uma conta corrente.
 * E a unica forma que carrega dados proprios (banco, agencia e conta). Os dados
 * sao finais: para mudar de conta cria-se um novo Banco.
 * A validacao dos campos (nao nulos e nao vazios) continua na Facade, antes
 * deste objeto ser criado.
 */
public class Banco implements MetodoPagamento {
    private final String banco;
    private final String agencia;
    private final String contaCorrente;

    public Banco(String banco, String agencia, String contaCorrente) {
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
    }

    @Override
    public String getNome() {
        return "banco";
    }

    @Override
    public String getBanco() {
        return banco;
    }

    @Override
    public String getAgencia() {
        return agencia;
    }

    @Override
    public String getContaCorrente() {
        return contaCorrente;
    }

    @Override
    public String descreverParaFolha(String enderecoDoEmpregado) {
        return banco + ", Ag. " + agencia + " CC " + contaCorrente;
    }
}