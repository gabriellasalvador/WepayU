package br.ufal.ic.p2.wepayu.models.agenda;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Agenda de pagamento mensal no ultimo dia util do mes (nao cai em sabado nem
 * domingo). E a agenda padrao dos assalariados e corresponde a descricao
 * "mensal $". O periodo pago e o mes inteiro.
 */
public class AgendaMensalUltimoDiaUtil implements AgendaPagamento {

    @Override
    public String getDescricao() {
        return "mensal $";
    }

    @Override
    public boolean isDiaDePagamento(LocalDate data) {
        return data.equals(ultimoDiaUtilDoMes(YearMonth.from(data)));
    }

    @Override
    public LocalDate inicioDoPeriodo(LocalDate dataDoPagamento) {
        // O periodo comeca no dia 1; "inicio" e o dia anterior a ele (ver AgendaPagamento).
        return YearMonth.from(dataDoPagamento).atDay(1).minusDays(1);
    }

    @Override
    public int diasNoPeriodo(LocalDate dataDoPagamento) {
        return YearMonth.from(dataDoPagamento).lengthOfMonth();
    }

    @Override
    public double parteFixaDoSalario(double salarioMensal) {
        return salarioMensal;
    }

    // Recua do ultimo dia do mes ate encontrar um dia que nao seja fim de semana.
    private LocalDate ultimoDiaUtilDoMes(YearMonth mes) {
        LocalDate dia = mes.atEndOfMonth();
        while (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.minusDays(1);
        }
        return dia;
    }
}