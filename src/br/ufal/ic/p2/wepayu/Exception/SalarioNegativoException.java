package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNegativoException extends ValidacaoException {
    public SalarioNegativoException() { super("Salario deve ser nao-negativo."); }
}
