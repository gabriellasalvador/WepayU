package br.ufal.ic.p2.wepayu.excecoes.tipoempregado;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;


/**
 * Lancada ao consultar atributos bancarios (banco, agencia, contaCorrente) de
 * um empregado cujo metodo de pagamento atual nao e "banco" (ver
 * Facade.getAtributoEmpregado, casos "banco"/"agencia"/"contaCorrente").
 */
public class EmpregadoNaoRecebeEmBancoException extends ValidacaoException {
    public EmpregadoNaoRecebeEmBancoException() { super("Empregado nao recebe em banco."); }
}