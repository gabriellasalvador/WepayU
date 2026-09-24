package br.ufal.ic.p2.wepayu.Exception.tipoempregado;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;


/**
 * Lancada ao consultar atributos bancarios (banco, agencia, contaCorrente) de
 * um empregado cujo metodo de pagamento atual nao e "banco" (ver
 * Facade.getAtributoEmpregado, casos "banco"/"agencia"/"contaCorrente").
 */
public class EmpregadoNaoRecebeEmBancoException extends ValidacaoException {
    public EmpregadoNaoRecebeEmBancoException() { super("Empregado nao recebe em banco."); }
}