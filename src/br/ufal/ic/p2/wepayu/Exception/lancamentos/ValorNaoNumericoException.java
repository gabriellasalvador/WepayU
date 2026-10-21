package br.ufal.ic.p2.wepayu.Exception.lancamentos;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada por lancaVenda e lancaTaxaServico quando o valor informado e nulo ou
 * nao pode ser convertido para numero (antes, esse caso estourava um
 * NumberFormatException sem tratamento).
 */
public class ValorNaoNumericoException extends ValidacaoException {
    public ValorNaoNumericoException() { super("Valor deve ser numerico."); }
}