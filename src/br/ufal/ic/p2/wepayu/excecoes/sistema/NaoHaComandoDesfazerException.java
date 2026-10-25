package br.ufal.ic.p2.wepayu.excecoes.sistema;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada por Facade.undo() quando a pilha de undo esta vazia, ou seja, nao
 * ha nenhum comando anterior para desfazer.
 */
public class NaoHaComandoDesfazerException extends ValidacaoException {
    public NaoHaComandoDesfazerException() {
        super("Nao ha comando a desfazer.");
    }
}
