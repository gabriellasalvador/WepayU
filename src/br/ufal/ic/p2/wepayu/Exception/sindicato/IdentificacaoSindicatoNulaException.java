package br.ufal.ic.p2.wepayu.Exception.sindicato;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o idSindicato informado ao sindicalizar um empregado
 * (Facade.alteraEmpregado, sobrecarga de sindicalizar) e nulo ou vazio.
 */
public class IdentificacaoSindicatoNulaException extends ValidacaoException {
    public IdentificacaoSindicatoNulaException() { super("Identificacao do sindicato nao pode ser nula."); }
}