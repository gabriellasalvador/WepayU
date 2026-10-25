package br.ufal.ic.p2.wepayu.models.agenda;

import br.ufal.ic.p2.wepayu.excecoes.agenda.AgendaJaExisteException;
import br.ufal.ic.p2.wepayu.excecoes.agenda.AgendaNaoDisponivelException;
import br.ufal.ic.p2.wepayu.excecoes.agenda.DescricaoAgendaInvalidaException;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Conjunto das agendas de pagamento que o sistema oferece para os empregados. Cada
 * agenda e identificada pela sua descricao ("semanal 5", "mensal $", "semanal 2 5").
 * Por padrao existem tres: a dos horistas, a dos assalariados e a dos comissionados.
 *
 * Um empregado so pode receber uma agenda que esteja neste catalogo. Assim, quem muda
 * a agenda de um empregado (a Facade) nao precisa conhecer as agendas existentes: pergunta
 * ao catalogo e recebe o objeto pronto ou a excecao de agenda indisponivel.
 */
public class CatalogoDeAgendas {
    // Chave: descricao da agenda. LinkedHashMap mantem a ordem em que as agendas foram incluidas.
    private Map<String, AgendaPagamento> agendas = new LinkedHashMap<>();

    public CatalogoDeAgendas() {
        redefinir();
    }

    /** Volta o catalogo para as tres agendas padrao (descarta as que tiverem sido criadas). */
    public void redefinir() {
        agendas = new LinkedHashMap<>();
        incluir(new AgendaSemanal(1, DayOfWeek.FRIDAY));
        incluir(new AgendaMensalUltimoDiaUtil());
        incluir(new AgendaSemanal(2, DayOfWeek.FRIDAY));
    }

    /**
     * Procura a agenda pela descricao exata.
     *
     * @throws AgendaNaoDisponivelException se nao houver agenda com essa descricao
     */
    public AgendaPagamento buscar(String descricao) throws AgendaNaoDisponivelException {
        AgendaPagamento agenda = agendas.get(descricao);
        if (agenda == null) throw new AgendaNaoDisponivelException();
        return agenda;
    }

    /**
     * Cria uma agenda nova a partir da descricao (ver FabricaAgenda) e a inclui no catalogo,
     * deixando-a disponivel para os empregados.
     *
     * @throws DescricaoAgendaInvalidaException se a descricao nao seguir nenhum formato aceito
     * @throws AgendaJaExisteException          se ja houver uma agenda igual (inclusive as padrao)
     */
    public void criar(String descricao) throws DescricaoAgendaInvalidaException, AgendaJaExisteException {
        AgendaPagamento nova = FabricaAgenda.criar(descricao);
        // A chave e a descricao padronizada da agenda ("semanal 1 5" e "semanal 5" sao a mesma).
        if (agendas.containsKey(nova.getDescricao())) throw new AgendaJaExisteException();
        incluir(nova);
    }

    private void incluir(AgendaPagamento agenda) {
        agendas.put(agenda.getDescricao(), agenda);
    }

    // ---------- Apoio ao undo/redo e a persistencia (fotos do estado) ----------

    /** Copia das agendas para ser gravada numa "foto" do estado do sistema. */
    public Map<String, AgendaPagamento> copiarAgendas() {
        return new LinkedHashMap<>(agendas);
    }

    /** Substitui as agendas pelas de uma "foto" restaurada. */
    public void restaurar(Map<String, AgendaPagamento> agendasRestauradas) {
        this.agendas = agendasRestauradas;
    }
}