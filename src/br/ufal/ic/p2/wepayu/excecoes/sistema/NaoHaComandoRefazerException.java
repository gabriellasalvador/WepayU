package br.ufal.ic.p2.wepayu.excecoes.sistema;


import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada por Facade.redo() quando a pilha de redo esta vazia, ou seja, nao
 * ha nenhum comando desfeito para refazer.
 */
public class NaoHaComandoRefazerException extends ValidacaoException {
    public NaoHaComandoRefazerException() {
        super("Nao ha comando a refazer.");
    }
}