package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o tipo informado nao combina com a sobrecarga de criarEmpregado
 * usada: passar "comissionado" na versao sem parametro de comissao, ou passar
 * um tipo diferente de "comissionado" na versao que exige comissao.
 */
public class TipoNaoAplicavelException extends ValidacaoException {
    public TipoNaoAplicavelException() { super("Tipo nao aplicavel."); }
}
