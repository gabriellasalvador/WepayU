package br.ufal.ic.p2.wepayu.models.folha;

/**
 * Resultado do calculo do pagamento de UM empregado em uma data: os valores de
 * cada coluna numerica da sua secao do relatorio (CategoriaFolha), na mesma
 * ordem das colunas. Os valores especificos de cada tipo de empregado vem
 * primeiro (ex.: horas e horas extras do horista), sendo o ultimo deles o
 * Salario Bruto; depois vem o desconto do sindicato e, por fim, o Salario Liquido
 * (bruto menos descontos).
 *
 * Objeto imutavel: so guarda numeros ja calculados, nao toma decisoes.
 */
public final class LinhaFolha {
    private final double[] valores;

    /**
     * @param parcelas  valores especificos do tipo de empregado; o ultimo e o salario bruto
     * @param descontos desconto do sindicato a aplicar sobre o bruto
     */
    public LinhaFolha(double[] parcelas, double descontos) {
        double bruto = parcelas[parcelas.length - 1];
        this.valores = new double[parcelas.length + 2];
        System.arraycopy(parcelas, 0, valores, 0, parcelas.length);
        valores[parcelas.length] = descontos;
        valores[parcelas.length + 1] = bruto - descontos;
    }

    /** Valores de todas as colunas (copia, para o objeto continuar imutavel). */
    public double[] getValores() {
        return valores.clone();
    }
}