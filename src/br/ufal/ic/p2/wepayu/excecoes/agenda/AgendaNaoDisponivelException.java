package br.ufal.ic.p2.wepayu.excecoes.agenda;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando se tenta dar a um empregado uma agenda de pagamento que nao existe
 * no sistema (nao e uma das agendas padrao nem foi criada por criarAgendaDePagamentos).
 */
public class AgendaNaoDisponivelException extends ValidacaoException {
    public AgendaNaoDisponivelException() { super("Agenda de pagamento nao esta disponivel"); }
}