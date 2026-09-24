package br.ufal.ic.p2.wepayu.Exception.cadastro;

import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

/**
 * Lancada por getEmpregadoPorNome quando o indice informado esta fora do
 * intervalo de empregados encontrados com o nome (substring) buscado.
 */
public class NaoHaEmpregadoComEsseNomeException extends ValidacaoException {
    public NaoHaEmpregadoComEsseNomeException() {
        super("Nao ha empregado com esse nome.");
    }
}