package br.ufal.ic.p2.wepayu.Exception.sindicato;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o valor de taxa sindical informado e nulo ou vazio, ao
 * sindicalizar um empregado.
 */
public class TaxaSindicalNulaException extends ValidacaoException {
    public TaxaSindicalNulaException() { super("Taxa sindical nao pode ser nula."); }
}
