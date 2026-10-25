package br.ufal.ic.p2.wepayu.excecoes.pagamento;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando a agencia informada e nula ou vazia, ao configurar o
 * pagamento em banco de um empregado (Facade.alteraEmpregado com banco/
 * agencia/contaCorrente).
 */
public class AgenciaNulaException extends ValidacaoException {
    public AgenciaNulaException() { super("Agencia nao pode ser nulo."); }
}
