package br.ufal.ic.p2.wepayu.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Grava e le em disco a foto do estado do sistema (arquivo estado.dat), para que os
 * dados sobrevivam entre uma execucao e outra. So cuida do arquivo; quem decide o que
 * entra na foto e como restaura-la e o GerenciadorDeEstado.
 */
public class ArmazenamentoDeEstado {
    private final Path arquivo;

    public ArmazenamentoDeEstado(String nomeDoArquivo) {
        this.arquivo = Paths.get(nomeDoArquivo);
    }

    /** Grava a foto no arquivo, substituindo o conteudo anterior. */
    public void salvar(EstadoDoSistema estado) {
        try {
            Files.write(arquivo, estado.getDados());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar estado: " + e.getMessage(), e);
        }
    }

    /** Le a foto salva; devolve vazio se o arquivo ainda nao existe (primeira execucao). */
    public Optional<EstadoDoSistema> carregar() {
        if (!Files.exists(arquivo)) return Optional.empty();
        try {
            return Optional.of(new EstadoDoSistema(Files.readAllBytes(arquivo)));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar estado salvo: " + e.getMessage(), e);
        }
    }
}