package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNuloException extends ValidacaoException {
    public SalarioNuloException() { super("Salario nao pode ser nulo."); }
}