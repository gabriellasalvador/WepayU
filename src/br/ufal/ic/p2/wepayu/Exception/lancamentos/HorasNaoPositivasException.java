package br.ufal.ic.p2.wepayu.Exception.lancamentos;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada por lancaCartao quando o numero de horas informado e zero ou
 * negativo (o cartao de ponto precisa registrar horas positivas).
 */
public class HorasNaoPositivasException extends ValidacaoException {
    public HorasNaoPositivasException() { super("Horas devem ser positivas."); }
}
