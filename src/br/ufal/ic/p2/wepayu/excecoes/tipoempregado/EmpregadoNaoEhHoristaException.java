package br.ufal.ic.p2.wepayu.excecoes.tipoempregado;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando uma operacao exclusiva de empregado horista (addCartao,
 * getCartoes) e chamada sobre um empregado de outro tipo. E o comportamento
 * padrao definido em Empregado para esses metodos, sobrescrito apenas em
 * EmpregadoHorista.
 */
public class EmpregadoNaoEhHoristaException extends ValidacaoException {
    public EmpregadoNaoEhHoristaException() { super("Empregado nao eh horista."); }
}
