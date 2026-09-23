package br.ufal.ic.p2.wepayu.Exception;

public class NaoHaEmpregadoComEsseNomeException extends ValidacaoException {
    public NaoHaEmpregadoComEsseNomeException() {
        super("Nao ha empregado com esse nome.");
    }
}