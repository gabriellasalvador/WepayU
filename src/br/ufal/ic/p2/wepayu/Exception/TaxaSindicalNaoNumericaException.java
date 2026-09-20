package br.ufal.ic.p2.wepayu.Exception;

public class TaxaSindicalNaoNumericaException extends ValidacaoException {
    public TaxaSindicalNaoNumericaException() { super("Taxa sindical deve ser numerica."); }
}
