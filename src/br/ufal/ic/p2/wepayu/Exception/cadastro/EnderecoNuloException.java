package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o endereco informado e nulo ou vazio, ao criar ou alterar
 * um empregado.
 */
public class EnderecoNuloException extends ValidacaoException {
    public EnderecoNuloException() { super("Endereco nao pode ser nulo."); }
}