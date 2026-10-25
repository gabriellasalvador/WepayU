package br.ufal.ic.p2.wepayu.util;

import java.util.Locale;

/**
 * Formatacao de numeros como texto no padrao usado pelos comandos e relatorios do
 * sistema (virgula como separador decimal). Antes estes metodos eram privados da
 * Facade; agora qualquer classe que precise exibir valores ou horas (como a folha
 * de pagamento) usa os mesmos formatos, sem depender da Facade.
 */
public final class Formatacao {

    private Formatacao() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    /**
     * Formata um double como valor monetario no padrao brasileiro (duas casas decimais,
     * separador decimal com virgula). Ex.: 1234.5 -> "1234,50".
     */
    public static String valor(double valor) {
        return String.format(Locale.US, "%.2f", valor).replace(".", ",");
    }

    /**
     * Formata um total de horas: se for um numero inteiro, exibe sem casas decimais;
     * caso contrario, exibe com uma ou duas casas decimais (removendo zero final desnecessario),
     * usando virgula como separador decimal.
     */
    public static String horas(double valor) {
        if (valor == Math.floor(valor)) {
            return String.valueOf((int) valor);
        }
        String formatado = String.format(Locale.US, "%.2f", valor).replace(".", ",");
        if (formatado.endsWith("0")) {
            formatado = formatado.substring(0, formatado.length() - 1);
        }
        return formatado;
    }
}