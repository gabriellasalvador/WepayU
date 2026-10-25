package br.ufal.ic.p2.wepayu.service;

/**
 * Resultado do calculo da folha de pagamento de uma data: o texto do relatorio e o
 * valor total pago. Objeto imutavel, so carrega os dois dados juntos.
 */
public final class ResultadoFolha {
    private final String relatorio;
    private final double total;

    public ResultadoFolha(String relatorio, double total) {
        this.relatorio = relatorio;
        this.total = total;
    }

    /** Texto completo do relatorio da folha, pronto para ser gravado em arquivo. */
    public String getRelatorio() {
        return relatorio;
    }

    /** Valor total pago na folha (soma dos salarios brutos). */
    public double getTotal() {
        return total;
    }
}