package br.ufal.ic.p2.wepayu.Exception.lancamentos;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando, num periodo de consulta, a data inicial e posterior a data
 * final (ex.: em getHorasNormaisTrabalhadas, getVendasRealizadas,
 * getTaxasServico), tornando o intervalo invalido.
 */
public class DataInicialPosteriorException extends ValidacaoException {
    public DataInicialPosteriorException() { super("Data inicial nao pode ser posterior aa data final."); }
}