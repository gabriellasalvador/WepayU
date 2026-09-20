package br.ufal.ic.p2.wepayu.Exception;

public class ComissaoNegativaException extends ValidacaoException {
    public ComissaoNegativaException() { super("Comissao deve ser nao-negativa."); }
}

