package br.ufal.ic.p2.wepayu.Exception.sindicato;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando a taxa sindical informada (ja convertida para numero) e
 * negativa, ao sindicalizar um empregado.
 */
public class TaxaSindicalNegativaException extends ValidacaoException {
    public TaxaSindicalNegativaException() { super("Taxa sindical deve ser nao-negativa."); }
}
