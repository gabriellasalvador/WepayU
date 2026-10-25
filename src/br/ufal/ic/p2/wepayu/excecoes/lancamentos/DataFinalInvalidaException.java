package br.ufal.ic.p2.wepayu.excecoes.lancamentos;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando a data final de um periodo de consulta (ex.: em
 * getHorasNormaisTrabalhadas, getVendasRealizadas, getTaxasServico) nao pode
 * ser convertida para uma data valida (Datas.converter falha).
 */
public class DataFinalInvalidaException extends ValidacaoException {
    public DataFinalInvalidaException() { super("Data final invalida."); }
}