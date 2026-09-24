package br.ufal.ic.p2.wepayu.Exception.pagamento;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o nome do banco informado e nulo ou vazio, ao configurar o
 * pagamento em banco de um empregado (Facade.alteraEmpregado com banco/
 * agencia/contaCorrente).
 */
public class BancoNuloException extends ValidacaoException {
    public BancoNuloException() { super("Banco nao pode ser nulo."); }
}