package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o valor de comissao informado nao pode ser convertido para
 * numero (falha no Double.parseDouble), ao criar ou alterar um empregado
 * comissionado.
 */
public class ComissaoNaoNumericaException extends ValidacaoException {
    public ComissaoNaoNumericaException() { super("Comissao deve ser numerica."); }
}
