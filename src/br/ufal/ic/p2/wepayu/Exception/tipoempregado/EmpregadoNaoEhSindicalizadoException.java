package br.ufal.ic.p2.wepayu.Exception.tipoempregado;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada ao consultar dados de sindicato (idSindicato, taxaSindical) ou
 * taxas de servico de um empregado que nao esta sindicalizado (ver
 * Facade.getAtributoEmpregado e Facade.getTaxasServico).
 */
public class EmpregadoNaoEhSindicalizadoException extends ValidacaoException {
    public EmpregadoNaoEhSindicalizadoException() { super("Empregado nao eh sindicalizado."); }
}
