package br.ufal.ic.p2.wepayu.models.agenda;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Agenda de pagamento mensal em um dia fixo do mes, de 1 a 28 (assim o dia existe em
 * todos os meses). Exemplo: "mensal 1" paga todo dia 1. O pagamento acontece nesse dia
 * mesmo que caia no fim de semana, e cobre o periodo desde o mesmo dia do mes anterior.
 * (Para pagar no ultimo dia util do mes existe a AgendaMensalUltimoDiaUtil, "mensal $".)
 */
public class AgendaMensalDia implements AgendaPagamento {
    private final int diaDoMes;

    /** @param diaDoMes dia do mes do pagamento, de 1 a 28 */
    public AgendaMensalDia(int diaDoMes) {
        this.diaDoMes = diaDoMes;
    }

    @Override
    public String getDescricao() {
        return "mensal " + diaDoMes;
    }

    @Override
    public boolean isDiaDePagamento(LocalDate data) {
        return data.getDayOfMonth() == diaDoMes;
    }

    @Override
    public LocalDate inicioDoPeriodo(LocalDate dataDoPagamento) {
        // O periodo comeca no dia seguinte ao pagamento anterior; "inicio" e o proprio
        // dia do pagamento anterior (ver AgendaPagamento).
        return dataDoPagamento.minusMonths(1);
    }

    @Override
    public int diasNoPeriodo(LocalDate dataDoPagamento) {
        return (int) ChronoUnit.DAYS.between(inicioDoPeriodo(dataDoPagamento), dataDoPagamento);
    }

    @Override
    public double parteFixaDoSalario(double salarioMensal) {
        return salarioMensal;
    }
}