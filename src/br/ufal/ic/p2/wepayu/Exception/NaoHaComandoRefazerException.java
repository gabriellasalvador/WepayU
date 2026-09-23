package br.ufal.ic.p2.wepayu.Exception;


public class NaoHaComandoRefazerException extends ValidacaoException {
    public NaoHaComandoRefazerException() {
        super("Nao ha comando a refazer.");
    }
}