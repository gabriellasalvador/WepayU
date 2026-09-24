package br.ufal.ic.p2.wepayu.Exception.sindicato;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o valor de taxa sindical informado nao pode ser convertido
 * para numero (falha no Double.parseDouble), ao sindicalizar um empregado.
 */
public class TaxaSindicalNaoNumericaException extends ValidacaoException {
    public TaxaSindicalNaoNumericaException() { super("Taxa sindical deve ser numerica."); }
}
