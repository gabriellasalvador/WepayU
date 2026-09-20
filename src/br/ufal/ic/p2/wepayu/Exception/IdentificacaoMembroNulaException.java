package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoMembroNulaException extends ValidacaoException {
    public IdentificacaoMembroNulaException() { super("Identificacao do membro nao pode ser nula."); }
}
