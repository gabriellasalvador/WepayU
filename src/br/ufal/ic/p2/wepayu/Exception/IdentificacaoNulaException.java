package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoNulaException extends ValidacaoException {
    public IdentificacaoNulaException() { super("Identificacao do empregado nao pode ser nula."); }
}