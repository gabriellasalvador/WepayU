package br.ufal.ic.p2.wepayu.Exception.lancamentos;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando a data inicial de um periodo de consulta (ex.: em
 * getHorasNormaisTrabalhadas, getVendasRealizadas, getTaxasServico) nao pode
 * ser convertida para uma data valida (Facade.parseData falha).
 */
public class DataInicialInvalidaException extends ValidacaoException {
    public DataInicialInvalidaException() { super("Data inicial invalida."); }
}