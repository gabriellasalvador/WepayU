package br.ufal.ic.p2.wepayu.excecoes.cadastro;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando o valor de salario informado e nulo ou vazio, ao criar ou
 * alterar um empregado.
 */
public class SalarioNuloException extends ValidacaoException {
    public SalarioNuloException() { super("Salario nao pode ser nulo."); }
}