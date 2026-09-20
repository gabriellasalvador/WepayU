package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoSindicatoNulaException extends ValidacaoException {
    public IdentificacaoSindicatoNulaException() { super("Identificacao do sindicato nao pode ser nula."); }
}