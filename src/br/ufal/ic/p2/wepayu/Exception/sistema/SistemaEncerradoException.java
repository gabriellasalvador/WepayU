package br.ufal.ic.p2.wepayu.Exception.sistema;


import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada por Facade.undo() e Facade.redo() quando o sistema ja foi encerrado
 * (encerrarSistema() foi chamado), impedindo qualquer comando adicional.
 */
public class SistemaEncerradoException extends ValidacaoException {
    public SistemaEncerradoException() {
        super("Nao pode dar comandos depois de encerrarSistema.");
    }
}
