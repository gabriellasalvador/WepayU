package br.ufal.ic.p2.wepayu.excecoes.tipoempregado;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando uma operacao exclusiva de empregado comissionado (getComissao,
 * setComissao, addVenda, getVendas) e chamada sobre um empregado de outro tipo.
 * E o comportamento padrao definido em Empregado para esses metodos, sobrescrito
 * apenas em EmpregadoComissionado.
 */
public class EmpregadoNaoEhComissionadoException extends ValidacaoException {
    public EmpregadoNaoEhComissionadoException() { super("Empregado nao eh comissionado."); }
}