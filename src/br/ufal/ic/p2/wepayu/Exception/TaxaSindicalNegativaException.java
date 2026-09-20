package br.ufal.ic.p2.wepayu.Exception;

public class TaxaSindicalNegativaException extends ValidacaoException {
    public TaxaSindicalNegativaException() { super("Taxa sindical deve ser nao-negativa."); }
}
