package br.ufal.ic.p2.wepayu.excecoes.sindicato;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando o id do membro (idSindicato) informado a um comando
 * relacionado a taxa de servico (lancaTaxaServico) e nulo ou vazio.
 */
public class IdentificacaoMembroNulaException extends ValidacaoException {
    public IdentificacaoMembroNulaException() { super("Identificacao do membro nao pode ser nula."); }
}
