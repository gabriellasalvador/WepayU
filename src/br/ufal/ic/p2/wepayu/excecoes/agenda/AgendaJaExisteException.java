package br.ufal.ic.p2.wepayu.excecoes.agenda;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada ao criar uma agenda de pagamento que ja existe no sistema, seja uma das
 * agendas padrao ou uma criada antes com a mesma descricao.
 */
public class AgendaJaExisteException extends ValidacaoException {
    public AgendaJaExisteException() { super("Agenda de pagamentos ja existe"); }
}