package br.ufal.ic.p2.wepayu.excecoes.sindicato;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando o idSindicato informado ao sindicalizar um empregado ja esta
 * em uso por outro empregado sindicalizado (Facade.alteraEmpregado, sobrecarga
 * de sindicalizar, que confere unicidade percorrendo todos os empregados).
 */
public class IdentificacaoSindicatoDuplicadaException extends ValidacaoException {
    public IdentificacaoSindicatoDuplicadaException() { super("Ha outro empregado com esta identificacao de sindicato"); }
}