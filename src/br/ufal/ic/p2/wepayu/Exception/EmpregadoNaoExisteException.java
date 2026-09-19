package br.ufal.ic.p2.wepayu.Exception;

public class EmpregadoNaoExisteException extends ValidacaoException {
    public EmpregadoNaoExisteException(){

        super("Empregado nao existe.");
    }
}
