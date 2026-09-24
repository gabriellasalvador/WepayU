package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o nome do atributo passado para getAtributoEmpregado ou
 * alteraEmpregado nao corresponde a nenhum atributo conhecido do empregado
 * (ver o switch/default em Facade.getAtributoEmpregado e Facade.alteraEmpregado).
 */
public class AtributoNaoExisteException extends ValidacaoException {
    public AtributoNaoExisteException() { super("Atributo nao existe."); }
}