package br.ufal.ic.p2.wepayu.models.folha;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Cada secao do relatorio da folha de pagamento (horistas, assalariados e
 * comissionados). Descreve apenas a APARENCIA da secao: titulo, rotulo do total,
 * largura da coluna de nomes e as colunas numericas. Quem informa a que secao
 * pertence um empregado e o proprio Empregado (Empregado.getCategoria()), e quem
 * calcula os valores de cada coluna e a classe do empregado
 * (Empregado.calcularLinhaFolha); a Facade so desenha a tabela.
 *
 * A ordem das constantes e a ordem em que as secoes aparecem no relatorio.
 * As tres ultimas colunas de toda categoria sao sempre Salario Bruto, Descontos
 * e Salario Liquido, nessa ordem.
 */
public enum CategoriaFolha {
    HORISTAS("HORISTAS", "TOTAL HORISTAS", 36,
            new Coluna("Horas", 5, true),
            new Coluna("Extra", 5, true),
            new Coluna("Salario Bruto", 13, false),
            new Coluna("Descontos", 9, false),
            new Coluna("Salario Liquido", 15, false)),

    ASSALARIADOS("ASSALARIADOS", "TOTAL ASSALARIADOS", 48,
            new Coluna("Salario Bruto", 13, false),
            new Coluna("Descontos", 9, false),
            new Coluna("Salario Liquido", 15, false)),

    COMISSIONADOS("COMISSIONADOS", "TOTAL COMISSIONADOS", 21,
            new Coluna("Fixo", 8, false),
            new Coluna("Vendas", 8, false),
            new Coluna("Comissao", 8, false),
            new Coluna("Salario Bruto", 13, false),
            new Coluna("Descontos", 9, false),
            new Coluna("Salario Liquido", 15, false));

    /** Uma coluna numerica da tabela: titulo, largura em caracteres e se o valor e exibido como horas. */
    public static final class Coluna {
        private final String titulo;
        private final int largura;
        private final boolean emHoras;

        public Coluna(String titulo, int largura, boolean emHoras) {
            this.titulo = titulo;
            this.largura = largura;
            this.emHoras = emHoras;
        }

        public String getTitulo() { return titulo; }
        public int getLargura() { return largura; }

        /** Verdadeiro se os valores desta coluna sao horas (e nao dinheiro). */
        public boolean isEmHoras() { return emHoras; }
    }

    private final String titulo;
    private final String rotuloDoTotal;
    private final int larguraDoNome;
    private final List<Coluna> colunas;

    CategoriaFolha(String titulo, String rotuloDoTotal, int larguraDoNome, Coluna... colunas) {
        this.titulo = titulo;
        this.rotuloDoTotal = rotuloDoTotal;
        this.larguraDoNome = larguraDoNome;
        this.colunas = Collections.unmodifiableList(Arrays.asList(colunas));
    }

    /** Titulo exibido na borda da secao (ex.: HORISTAS). */
    public String getTitulo() { return titulo; }

    /** Texto da linha de total da secao (ex.: TOTAL HORISTAS). */
    public String getRotuloDoTotal() { return rotuloDoTotal; }

    /** Largura da primeira coluna (nome do empregado). */
    public int getLarguraDoNome() { return larguraDoNome; }

    /** Colunas numericas da secao, na ordem em que aparecem. */
    public List<Coluna> getColunas() { return colunas; }

    /** Posicao da coluna Salario Bruto (antes de Descontos e Salario Liquido). */
    public int getIndiceDoBruto() { return colunas.size() - 3; }
}