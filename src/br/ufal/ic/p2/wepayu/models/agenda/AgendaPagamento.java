package br.ufal.ic.p2.wepayu.models.agenda;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Estrategia (padrao Strategy) que define QUANDO um empregado e pago e QUAL
 * periodo de trabalho cada pagamento cobre. Nao depende do tipo do empregado:
 * qualquer empregado pode ter qualquer agenda (um horista pode receber por mes,
 * um assalariado por semana etc.); o tipo so decide COMO calcular o valor.
 *
 * Cada agenda e uma classe propria (AgendaSemanal, AgendaMensalUltimoDiaUtil),
 * entao nem o Empregado nem a Facade precisam saber qual e a regra: basta
 * perguntar. Criar uma nova agenda e escrever uma nova classe.
 *
 * Implementa Serializable porque o Empregado guarda uma instancia dela e e
 * serializado nas "fotos" de undo/redo e na persistencia (estado.dat).
 * As implementacoes sao imutaveis.
 */
public interface AgendaPagamento extends Serializable {

    /**
     * Descricao da agenda, no formato usado pelos comandos do sistema
     * (por exemplo "semanal 5", "semanal 2 5" ou "mensal $").
     */
    String getDescricao();

    /** Informa se o empregado recebe pagamento na data informada. */
    boolean isDiaDePagamento(LocalDate data);

    /**
     * Dia imediatamente anterior ao primeiro dia coberto pelo pagamento que
     * acontece em "dataDoPagamento". O periodo pago e, portanto, o intervalo
     * (inicioDoPeriodo, dataDoPagamento], sem o primeiro dia e com o ultimo.
     */
    LocalDate inicioDoPeriodo(LocalDate dataDoPagamento);

    /**
     * Quantidade de dias do periodo pago em "dataDoPagamento"; e a base de
     * calculo da taxa sindical diaria.
     */
    int diasNoPeriodo(LocalDate dataDoPagamento);

    /**
     * Parte do salario mensal que corresponde a um periodo desta agenda. E o
     * valor pago a empregados de salario fixo (assalariados e a parte fixa dos
     * comissionados): um mes inteiro no pagamento mensal, ou 12/52 do salario
     * mensal por semana de periodo no pagamento semanal.
     */
    double parteFixaDoSalario(double salarioMensal);
}