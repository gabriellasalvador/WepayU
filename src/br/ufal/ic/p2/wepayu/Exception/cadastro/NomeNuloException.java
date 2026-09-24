package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o nome informado e nulo ou vazio, ao criar ou alterar
 * um empregado.
 */
public class NomeNuloException extends ValidacaoException {
    public NomeNuloException() { super("Nome nao pode ser nulo."); }
}