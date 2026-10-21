package br.ufal.ic.p2.wepayu.models.pagamento;

/**
 * Forma de pagamento "correios": o cheque e enviado pelo correio para o
 * endereco cadastrado do empregado. O endereco nao e guardado aqui (continua
 * sendo do Empregado); ele chega por parametro na hora de descrever o metodo,
 * assim, se o empregado mudar de endereco, o relatorio ja sai atualizado.
 */
public class Correios implements MetodoPagamento {

    @Override
    public String getNome() {
        return "correios";
    }

    @Override
    public String descreverParaFolha(String enderecoDoEmpregado) {
        return "Correios, " + enderecoDoEmpregado;
    }
}