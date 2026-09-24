package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o salario informado (ja convertido para numero) e negativo,
 * ao criar ou alterar um empregado.
 */
public class SalarioNegativoException extends ValidacaoException {
    public SalarioNegativoException() { super("Salario deve ser nao-negativo."); }
}
