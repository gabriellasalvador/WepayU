package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o valor de salario informado nao pode ser convertido para
 * numero (falha no Double.parseDouble), ao criar ou alterar um empregado.
 */
public class SalarioNaoNumericoException extends ValidacaoException {
    public SalarioNaoNumericoException() { super("Salario deve ser numerico."); }
}