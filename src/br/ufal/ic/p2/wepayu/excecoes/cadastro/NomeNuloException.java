package br.ufal.ic.p2.wepayu.excecoes.cadastro;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando o nome informado e nulo ou vazio, ao criar ou alterar
 * um empregado.
 */
public class NomeNuloException extends ValidacaoException {
    public NomeNuloException() { super("Nome nao pode ser nulo."); }
}