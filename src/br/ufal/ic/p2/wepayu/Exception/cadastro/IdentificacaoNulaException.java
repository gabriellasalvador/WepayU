package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;


/**
 * Lancada quando o id do empregado informado a um comando (ex.: lancaCartao,
 * removerEmpregado, getAtributoEmpregado) e nulo ou vazio.
 */
public class IdentificacaoNulaException extends ValidacaoException {
    public IdentificacaoNulaException() { super("Identificacao do empregado nao pode ser nula."); }
}