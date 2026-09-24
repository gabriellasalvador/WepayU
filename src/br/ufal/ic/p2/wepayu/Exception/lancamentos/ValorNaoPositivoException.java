package br.ufal.ic.p2.wepayu.Exception.lancamentos;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada por lancaVenda e lancaTaxaServico quando o valor informado e zero
 * ou negativo (vendas e taxas de servico precisam ter valor positivo).
 */
public class ValorNaoPositivoException extends ValidacaoException {
    public ValorNaoPositivoException() { super("Valor deve ser positivo."); }
}