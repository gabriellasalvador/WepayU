package br.ufal.ic.p2.wepayu.Exception.lancamentos;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando a data informada em um lancamento pontual (lancaCartao,
 * lancaVenda, lancaTaxaServico) nao pode ser convertida para uma data valida
 * (Facade.parseData falha).
 */
public class DataInvalidaException extends ValidacaoException {
    public DataInvalidaException() { super("Data invalida."); }
}