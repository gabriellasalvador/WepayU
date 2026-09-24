package br.ufal.ic.p2.wepayu.Exception.tipoempregado;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando uma operacao exclusiva de empregado horista (addCartao,
 * getCartoes) e chamada sobre um empregado de outro tipo. E o comportamento
 * padrao definido em Empregado para esses metodos, sobrescrito apenas em
 * EmpregadoHorista.
 */
public class EmpregadoNaoEhHoristaException extends ValidacaoException {
    public EmpregadoNaoEhHoristaException() { super("Empregado nao eh horista."); }
}
