package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;
import br.ufal.ic.p2.wepayu.excecoes.lancamentos.DataFinalInvalidaException;
import br.ufal.ic.p2.wepayu.excecoes.lancamentos.DataInicialInvalidaException;
import br.ufal.ic.p2.wepayu.excecoes.lancamentos.DataInicialPosteriorException;
import br.ufal.ic.p2.wepayu.util.Datas;

import java.time.LocalDate;

/**
 * Intervalo de datas [inicio, fim): inclui a data inicial e NAO inclui a final. E o
 * periodo usado nas consultas de horas, vendas e taxas de servico. Concentra a
 * conversao das duas datas, a checagem de ordem e o teste "a data esta dentro do
 * periodo".
 */
public final class Periodo {
    private final LocalDate inicio;
    private final LocalDate fim;

    private Periodo(LocalDate inicio, LocalDate fim) {
        this.inicio = inicio;
        this.fim = fim;
    }

    /**
     * Cria o periodo a partir de duas datas em texto, validando na ordem: data inicial,
     * data final e, por fim, se a inicial nao e posterior a final.
     */
    public static Periodo de(String dataInicial, String dataFinal) throws ValidacaoException {
        LocalDate inicio = Datas.converter(dataInicial, DataInicialInvalidaException::new);
        LocalDate fim = Datas.converter(dataFinal, DataFinalInvalidaException::new);
        if (inicio.isAfter(fim)) throw new DataInicialPosteriorException();
        return new Periodo(inicio, fim);
    }

    /** Diz se a data esta dentro do periodo: a partir da inicial, ate antes da final. */
    public boolean contem(LocalDate data) {
        return !data.isBefore(inicio) && data.isBefore(fim);
    }
}