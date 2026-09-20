package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoEhComissionadoException extends ValidacaoException {
    public EmpregadoNaoEhComissionadoException() { super("Empregado nao eh comissionado."); }
}