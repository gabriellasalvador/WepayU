package br.ufal.ic.p2.wepayu.Exception.pagamento;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando a agencia informada e nula ou vazia, ao configurar o
 * pagamento em banco de um empregado (Facade.alteraEmpregado com banco/
 * agencia/contaCorrente).
 */
public class AgenciaNulaException extends ValidacaoException {
    public AgenciaNulaException() { super("Agencia nao pode ser nulo."); }
}
