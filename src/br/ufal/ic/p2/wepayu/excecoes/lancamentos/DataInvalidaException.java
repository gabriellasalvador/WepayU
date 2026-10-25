package br.ufal.ic.p2.wepayu.excecoes.lancamentos;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando a data informada em um lancamento pontual (lancaCartao,
 * lancaVenda, lancaTaxaServico) nao pode ser convertida para uma data valida
 * (Datas.converter falha).
 */
public class DataInvalidaException extends ValidacaoException {
    public DataInvalidaException() { super("Data invalida."); }
}