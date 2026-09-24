package br.ufal.ic.p2.wepayu.Exception.cadastro;

/**
 * Lancada quando o id de empregado informado nao corresponde a nenhum
 * empregado cadastrado no sistema (usada em varios comandos da Facade que
 * buscam um empregado por id, ex.: getAtributoEmpregado, lancaCartao,
 * removerEmpregado, alteraEmpregado).
 */

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

public class EmpregadoNaoExisteException extends ValidacaoException {
    public EmpregadoNaoExisteException(){

        super("Empregado nao existe.");
    }
}
