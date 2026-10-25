package br.ufal.ic.p2.wepayu.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Grava um relatorio em texto em um arquivo (codificacao UTF-8). So cuida da escrita em
 * disco; o conteudo do relatorio vem pronto do FolhaDePagamento.
 */
public class EscritorDeRelatorio {

    /**
     * Escreve o texto no arquivo indicado, substituindo o conteudo anterior.
     *
     * @throws RuntimeException se o arquivo nao puder ser escrito (a causa original e mantida)
     */
    public void escrever(String caminhoDoArquivo, String relatorio) {
        try (PrintWriter saida = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(caminhoDoArquivo), "UTF-8"))) {
            saida.print(relatorio);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever arquivo da folha: " + e.getMessage(), e);
        }
    }
}