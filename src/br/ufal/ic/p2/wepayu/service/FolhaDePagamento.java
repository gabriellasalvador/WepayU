package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.folha.CategoriaFolha;
import br.ufal.ic.p2.wepayu.repository.RepositorioEmpregados;
import br.ufal.ic.p2.wepayu.util.Dinheiro;
import br.ufal.ic.p2.wepayu.util.Formatacao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servico responsavel pela folha de pagamento: percorre os empregados do repositorio,
 * pede a cada um o calculo do seu pagamento (Empregado.calcularLinhaFolha) e monta o
 * relatorio em texto com a tabela de cada secao e os totais.
 *
 * Antes toda essa logica estava dentro da Facade. Agora a Facade so recebe o comando
 * (totalFolha ou rodaFolha), chama este servico e usa o resultado, o que deixa cada
 * classe com uma unica responsabilidade.
 *
 * O servico tambem guarda as folhas ja geradas por data (cache), que fazem parte do
 * estado do sistema e por isso entram nas fotos de undo/redo (GerenciadorDeEstado).
 */
public class FolhaDePagamento {
    private final RepositorioEmpregados repositorio;
    // Cache das folhas ja geradas por data, para que gerarRelatorio() nao recalcule
    // (nem remova saldo de divida sindical duas vezes) se chamado de novo com a mesma data.
    private Map<LocalDate, String> folhasGeradas = new HashMap<>();

    public FolhaDePagamento(RepositorioEmpregados repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Gera de fato a folha de uma data (persistindo os descontos sindicais calculados)
     * e devolve o texto do relatorio. Se a folha dessa data ja foi gerada antes,
     * devolve o mesmo relatorio sem recalcular nem descontar a divida sindical de novo.
     */
    public String gerarRelatorio(LocalDate data) {
        return folhasGeradas.computeIfAbsent(data, d -> calcular(d, true).getRelatorio());
    }

    /** Esquece as folhas ja geradas (usado ao zerar o sistema). */
    public void limparFolhasGeradas() {
        folhasGeradas.clear();
    }

    /** Copia das folhas geradas, para entrar numa foto do estado do sistema. */
    public Map<LocalDate, String> copiarFolhasGeradas() {
        return new HashMap<>(folhasGeradas);
    }

    /** Substitui as folhas geradas pelas de uma foto restaurada. */
    public void restaurarFolhasGeradas(Map<LocalDate, String> folhasRestauradas) {
        this.folhasGeradas = folhasRestauradas;
    }

    /**
     * Monta uma string repetindo o caractere c, n vezes. Usado para desenhar as
     * linhas de borda (=====) do relatorio de folha.
     */
    private String repete(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    /**
     * Linha de borda "cheia" (127 caracteres de '=') usada no topo/rodape de cada
     * secao do relatorio de folha.
     */
    private String bordaTopo() {
        return repete('=', 127);
    }

    /**
     * Linha de borda com o titulo da categoria centralizado dentro de '=' (ex.: HORISTAS),
     * completando ate 127 caracteres.
     */
    private String bordaCategoria(String titulo) {
        String meio = repete('=', 21) + " " + titulo + " ";
        return meio + repete('=', 127 - meio.length());
    }

    /**
     * Linha de cabecalho da tabela de uma secao: nomes das colunas alinhados a esquerda.
     */
    private String formatarCabecalho(CategoriaFolha categoria) {
        StringBuilder sb = new StringBuilder(String.format("%-" + categoria.getLarguraDoNome() + "s", "Nome"));
        for (CategoriaFolha.Coluna coluna : categoria.getColunas()) {
            sb.append(' ').append(String.format("%-" + coluna.getLargura() + "s", coluna.getTitulo()));
        }
        return sb.append(" Metodo").toString();
    }

    /**
     * Linha de '=' logo abaixo do cabecalho, com a largura de cada coluna (38 para o metodo).
     */
    private String formatarSeparador(CategoriaFolha categoria) {
        StringBuilder sb = new StringBuilder(repete('=', categoria.getLarguraDoNome()));
        for (CategoriaFolha.Coluna coluna : categoria.getColunas()) {
            sb.append(' ').append(repete('=', coluna.getLargura()));
        }
        return sb.append(' ').append(repete('=', 38)).toString();
    }

    /**
     * Formata os valores de uma linha (ou do total) da tabela: o texto inicial alinhado
     * a esquerda na coluna de nomes e cada valor alinhado a direita na sua coluna.
     * Valores de colunas de horas usam Formatacao.horas; os demais, Formatacao.valor.
     */
    private String formatarValores(CategoriaFolha categoria, String texto, double[] valores) {
        StringBuilder sb = new StringBuilder(String.format("%-" + categoria.getLarguraDoNome() + "s", texto));
        for (int i = 0; i < valores.length; i++) {
            CategoriaFolha.Coluna coluna = categoria.getColunas().get(i);
            String valor = coluna.isEmHoras() ? Formatacao.horas(valores[i]) : Formatacao.valor(valores[i]);
            sb.append(' ').append(String.format("%" + coluna.getLargura() + "s", valor));
        }
        return sb.toString();
    }

    /**
     * Calcula (e formata como texto) a folha de pagamento completa referente a uma data.
     * Para cada secao (CategoriaFolha), lista os empregados dela em ordem alfabetica e,
     * para os que recebem naquela data segundo a propria agenda de pagamento, pede ao
     * empregado o calculo da sua linha (Empregado.calcularLinhaFolha). Esta classe so
     * monta a tabela e os totais; nao sabe como cada tipo calcula o salario.
     * aplicarMutacao controla se o desconto sindical calculado deve ser persistido
     * (rodaFolha) ou apenas simulado (totalFolha).
     */
    public ResultadoFolha calcular(LocalDate data, boolean aplicarMutacao) {
        StringBuilder sb = new StringBuilder();

        String titulo = "FOLHA DE PAGAMENTO DO DIA " + data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        sb.append(titulo).append("\r\n");
        sb.append(repete('=', titulo.length())).append("\r\n");
        sb.append("\r\n");

        double totalGeral = 0;

        for (CategoriaFolha categoria : CategoriaFolha.values()) {
            List<Empregado> membros = new ArrayList<>();
            for (Empregado e : repositorio.listar()) if (e.getCategoria() == categoria) membros.add(e);
            membros.sort(Comparator.comparing(Empregado::getNome));

            sb.append(bordaTopo()).append("\r\n");
            sb.append(bordaCategoria(categoria.getTitulo())).append("\r\n");
            sb.append(bordaTopo()).append("\r\n");
            sb.append(formatarCabecalho(categoria)).append("\r\n");
            sb.append(formatarSeparador(categoria)).append("\r\n");

            double[] totais = new double[categoria.getColunas().size()];
            for (Empregado e : membros) {
                if (!e.getAgenda().isDiaDePagamento(data)) continue;

                double[] valores = e.calcularLinhaFolha(data, aplicarMutacao).getValores();
                sb.append(formatarValores(categoria, e.getNome(), valores))
                        .append(' ').append(e.getMetodoPagamento().descreverParaFolha(e.getEndereco()))
                        .append("\r\n");

                for (int i = 0; i < totais.length; i++) totais[i] += valores[i];
            }
            sb.append("\r\n");
            sb.append(formatarValores(categoria, categoria.getRotuloDoTotal(), totais)).append("\r\n");
            sb.append("\r\n");
            totalGeral += totais[categoria.getIndiceDoBruto()];
        }

        double total = Dinheiro.truncar(totalGeral);
        sb.append("TOTAL FOLHA: ").append(Formatacao.valor(total)).append("\r\n");

        return new ResultadoFolha(sb.toString(), total);
    }
}