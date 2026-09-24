package br.ufal.ic.p2.wepayu.Exception.sindicato;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada por Facade.buscarPorIdSindicato quando nenhum empregado
 * sindicalizado tem o idSindicato ("membro") informado, tipicamente ao
 * lancar uma taxa de servico.
 */
public class MembroNaoExisteException extends ValidacaoException {
    public MembroNaoExisteException() { super("Membro nao existe."); }
}
