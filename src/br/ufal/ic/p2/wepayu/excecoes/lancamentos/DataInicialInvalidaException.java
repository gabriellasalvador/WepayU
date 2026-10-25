package br.ufal.ic.p2.wepayu.excecoes.lancamentos;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando a data inicial de um periodo de consulta (ex.: em
 * getHorasNormaisTrabalhadas, getVendasRealizadas, getTaxasServico) nao pode
 * ser convertida para uma data valida (Datas.converter falha).
 */
public class DataInicialInvalidaException extends ValidacaoException {
    public DataInicialInvalidaException() { super("Data inicial invalida."); }
}