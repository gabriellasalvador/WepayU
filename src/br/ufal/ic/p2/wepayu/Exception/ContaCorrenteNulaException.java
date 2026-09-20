package br.ufal.ic.p2.wepayu.Exception;

public class ContaCorrenteNulaException extends ValidacaoException {
    public ContaCorrenteNulaException() { super("Conta corrente nao pode ser nulo."); }
}
