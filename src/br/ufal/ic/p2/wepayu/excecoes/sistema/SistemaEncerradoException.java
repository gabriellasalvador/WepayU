package br.ufal.ic.p2.wepayu.excecoes.sistema;


import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada pela Facade quando qualquer comando e dado depois de encerrarSistema()
 * (inclusive consultas e undo/redo).
 */
public class SistemaEncerradoException extends ValidacaoException {
    public SistemaEncerradoException() {
        super("Nao pode dar comandos depois de encerrarSistema.");
    }
}