package br.ufal.ic.p2.wepayu.excecoes.lancamentos;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada por lancaCartao quando o valor de horas informado e nulo ou nao
 * pode ser convertido para numero (antes, esse caso estourava um
 * NumberFormatException sem tratamento).
 */
public class HorasNaoNumericasException extends ValidacaoException {
    public HorasNaoNumericasException() { super("Horas devem ser numericas."); }
}