package br.ufal.ic.p2.wepayu.Exception.sindicato;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada quando o valor informado para o atributo sindicalizado nao e
 * "false" (Facade.alteraEmpregado so aceita desfazer a sindicalizacao por
 * esse atributo; sindicalizar exige a sobrecarga com idSindicato/taxaSindical).
 */
public class ValorSindicalizadoInvalidoException extends ValidacaoException {
    public ValorSindicalizadoInvalidoException() { super("Valor deve ser true ou false."); }
}