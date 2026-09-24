package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando a comissao informada (ja convertida para numero) e negativa,
 * ao criar ou alterar um empregado comissionado.
 */
public class ComissaoNegativaException extends ValidacaoException {
    public ComissaoNegativaException() { super("Comissao deve ser nao-negativa."); }
}

