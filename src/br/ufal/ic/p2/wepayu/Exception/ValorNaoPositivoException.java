package br.ufal.ic.p2.wepayu.Exception;

public class ValorNaoPositivoException extends ValidacaoException {
    public ValorNaoPositivoException() { super("Valor deve ser positivo."); }
}