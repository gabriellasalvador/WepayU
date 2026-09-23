package br.ufal.ic.p2.wepayu.Exception;

public class NaoHaComandoDesfazerException extends ValidacaoException {
    public NaoHaComandoDesfazerException() {
        super("Nao ha comando a desfazer.");
    }
}
