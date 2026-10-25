package br.ufal.ic.p2.wepayu.excecoes.pagamento;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando a conta corrente informada e nula ou vazia, ao configurar o
 * pagamento em banco de um empregado (Facade.alteraEmpregado com banco/
 * agencia/contaCorrente).
 */
public class ContaCorrenteNulaException extends ValidacaoException {
    public ContaCorrenteNulaException() { super("Conta corrente nao pode ser nulo."); }
}
