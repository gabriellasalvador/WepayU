package br.ufal.ic.p2.wepayu.excecoes.cadastro;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;


/**
 * Lancada quando o valor de comissao informado e nulo ou vazio, ao criar ou
 * alterar um empregado comissionado.
 */
public class ComissaoNulaException extends ValidacaoException {
    public ComissaoNulaException() { super("Comissao nao pode ser nula."); }
}