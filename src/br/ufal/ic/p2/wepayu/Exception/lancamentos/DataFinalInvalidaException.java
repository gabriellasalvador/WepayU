package br.ufal.ic.p2.wepayu.Exception.lancamentos;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando a data final de um periodo de consulta (ex.: em
 * getHorasNormaisTrabalhadas, getVendasRealizadas, getTaxasServico) nao pode
 * ser convertida para uma data valida (Facade.parseData falha).
 */
public class DataFinalInvalidaException extends ValidacaoException {
    public DataFinalInvalidaException() { super("Data final invalida."); }
}
