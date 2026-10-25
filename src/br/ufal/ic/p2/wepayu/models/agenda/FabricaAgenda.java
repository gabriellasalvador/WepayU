package br.ufal.ic.p2.wepayu.models.agenda;

import br.ufal.ic.p2.wepayu.excecoes.agenda.DescricaoAgendaInvalidaException;

import java.time.DayOfWeek;

/**
 * Fabrica que transforma a descricao de uma agenda de pagamento (texto) no objeto
 * correspondente (padrao Factory). Formatos aceitos:
 *
 *  - "semanal D": toda semana, no dia D da semana (1 = segunda-feira ... 7 = domingo);
 *  - "semanal N D": a cada N semanas (1 a 52), no dia D da semana (1 a 7);
 *  - "mensal D": todo mes no dia D (1 a 28);
 *  - "mensal $": todo mes no ultimo dia util.
 *
 * Qualquer outro texto resulta em DescricaoAgendaInvalidaException.
 */
public final class FabricaAgenda {

    private FabricaAgenda() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    public static AgendaPagamento criar(String descricao) throws DescricaoAgendaInvalidaException {
        if (descricao == null) throw new DescricaoAgendaInvalidaException();
        String[] partes = descricao.trim().split("\\s+");

        if (partes[0].equals("semanal") && partes.length == 2) {
            return new AgendaSemanal(1, diaDaSemana(partes[1]));
        }
        if (partes[0].equals("semanal") && partes.length == 3) {
            int intervalo = numeroEntre(partes[1], 1, 52);
            return new AgendaSemanal(intervalo, diaDaSemana(partes[2]));
        }
        if (partes[0].equals("mensal") && partes.length == 2) {
            if (partes[1].equals("$")) return new AgendaMensalUltimoDiaUtil();
            return new AgendaMensalDia(numeroEntre(partes[1], 1, 28));
        }
        throw new DescricaoAgendaInvalidaException();
    }

    private static DayOfWeek diaDaSemana(String texto) throws DescricaoAgendaInvalidaException {
        return DayOfWeek.of(numeroEntre(texto, 1, 7));
    }

    /** Converte o texto em inteiro e confere se esta no intervalo [minimo, maximo]. */
    private static int numeroEntre(String texto, int minimo, int maximo) throws DescricaoAgendaInvalidaException {
        try {
            int numero = Integer.parseInt(texto);
            if (numero < minimo || numero > maximo) throw new DescricaoAgendaInvalidaException();
            return numero;
        } catch (NumberFormatException e) {
            throw new DescricaoAgendaInvalidaException();
        }
    }
}