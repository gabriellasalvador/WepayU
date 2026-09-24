package br.ufal.ic.p2.wepayu.Exception.pagamento;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o valor informado para o atributo metodoPagamento nao e
 * "emMaos" nem "correios" (ver Facade.alteraEmpregado, caso "metodoPagamento";
 * o caso "banco" e tratado por uma sobrecarga separada, que exige
 * banco/agencia/contaCorrente).
 */
public class MetodoPagamentoInvalidoException extends ValidacaoException {
    public MetodoPagamentoInvalidoException() { super("Metodo de pagamento invalido."); }
}
