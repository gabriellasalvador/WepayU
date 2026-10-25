package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.lancamentos.HorasNaoPositivasException;

import java.time.LocalDate;
import java.io.Serializable;

/**
 * Registro de um cartao de ponto: quantas horas um empregado horista trabalhou
 * em uma data especifica. Cada lancaCartao(...) na Facade cria um destes e
 * adiciona a lista de cartoes do empregado (EmpregadoHorista.cartoes).
 *
 * Implementa Serializable porque faz parte do estado do sistema salvo pela
 * Facade nas "fotos" usadas no undo/redo/persistencia.
 */
public class CartaoDePonto implements Serializable{
    // Ate esta quantidade de horas no dia, o tempo e normal; o que passar e hora extra.
    private static final double LIMITE_HORAS_NORMAIS = 8;

    private LocalDate data;
    private double horas;

    /**
     * @throws HorasNaoPositivasException se as horas forem zero ou negativas
     */
    public CartaoDePonto(LocalDate data, double horas) throws HorasNaoPositivasException {
        if (horas <= 0) throw new HorasNaoPositivasException();
        this.data = data;
        this.horas = horas;
    }

    public LocalDate getData() {
        return data;
    }

    public double getHoras() {
        return horas;
    }

    /**
     * Horas normais deste cartao: ate 8 horas por dia. Regra do horista, que fica
     * aqui para o resto do sistema nao precisar conhecer o limite.
     */
    public double getHorasNormais() {
        return Math.min(horas, LIMITE_HORAS_NORMAIS);
    }

    /** Horas extras deste cartao: o que passar de 8 horas no dia (nunca negativo). */
    public double getHorasExtras() {
        return Math.max(0, horas - LIMITE_HORAS_NORMAIS);
    }
}