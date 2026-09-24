package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;


/**
 * Lancada quando o tipo informado nao e "horista", "assalariado" nem
 * "comissionado", ao criar um empregado ou trocar seu tipo (Facade.trocarTipo).
 */
public class TipoInvalidoException extends ValidacaoException {
    public TipoInvalidoException() { super("Tipo invalido."); }
}