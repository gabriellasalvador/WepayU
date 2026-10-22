package br.ufal.ic.p2.wepayu.models.agenda;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Agenda de pagamento semanal: o empregado e pago em um dia fixo da semana, a
 * cada "intervaloEmSemanas" semanas. Exemplos: "semanal 5" (toda sexta-feira,
 * agenda padrao dos horistas) e "semanal 2 5" (sexta-feira sim, sexta-feira nao,
 * agenda padrao dos comissionados).
 *
 * Quando o intervalo e maior que uma semana, o ciclo comeca na ocorrencia numero
 * "intervalo" do dia da semana a partir de 01/01/2005; para a sexta-feira de 2
 * em 2 semanas isso resulta em 14/01/2005, 28/01/2005 e assim por diante.
 */
public class AgendaSemanal implements AgendaPagamento {
    // Data a partir da qual se conta o ciclo de pagamentos quando o intervalo e maior que 1 semana.
    private static final LocalDate INICIO_DO_CICLO = LocalDate.of(2005, 1, 1);

    private final int intervaloEmSemanas;
    private final DayOfWeek diaDaSemana;

    /**
     * @param intervaloEmSemanas de quantas em quantas semanas o pagamento acontece (1 = toda semana)
     * @param diaDaSemana        dia da semana em que o pagamento acontece
     */
    public AgendaSemanal(int intervaloEmSemanas, DayOfWeek diaDaSemana) {
        this.intervaloEmSemanas = intervaloEmSemanas;
        this.diaDaSemana = diaDaSemana;
    }

    @Override
    public String getDescricao() {
        if (intervaloEmSemanas == 1) return "semanal " + diaDaSemana.getValue();
        return "semanal " + intervaloEmSemanas + " " + diaDaSemana.getValue();
    }

    @Override
    public boolean isDiaDePagamento(LocalDate data) {
        if (data.getDayOfWeek() != diaDaSemana) return false;
        if (intervaloEmSemanas == 1) return true;

        LocalDate primeiroPagamento = INICIO_DO_CICLO
                .with(TemporalAdjusters.nextOrSame(diaDaSemana))
                .plusWeeks(intervaloEmSemanas - 1);
        if (data.isBefore(primeiroPagamento)) return false;
        return ChronoUnit.DAYS.between(primeiroPagamento, data) % (7L * intervaloEmSemanas) == 0;
    }

    @Override
    public LocalDate inicioDoPeriodo(LocalDate dataDoPagamento) {
        return dataDoPagamento.minusDays(diasNoPeriodo(dataDoPagamento));
    }

    @Override
    public int diasNoPeriodo(LocalDate dataDoPagamento) {
        return 7 * intervaloEmSemanas;
    }

    @Override
    public double parteFixaDoSalario(double salarioMensal) {
        // O ano tem 52 semanas e 12 meses: cada semana equivale a 12/52 do salario mensal.
        return salarioMensal * 12 * intervaloEmSemanas / 52;
    }
}