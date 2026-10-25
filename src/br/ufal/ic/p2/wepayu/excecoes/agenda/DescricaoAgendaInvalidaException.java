package br.ufal.ic.p2.wepayu.excecoes.agenda;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

/**
 * Lancada quando a descricao de uma nova agenda de pagamento nao segue nenhum dos
 * formatos aceitos: "semanal D", "semanal N D" ou "mensal D" (ver FabricaAgenda).
 */
public class DescricaoAgendaInvalidaException extends ValidacaoException {
    public DescricaoAgendaInvalidaException() { super("Descricao de agenda invalida"); }
}