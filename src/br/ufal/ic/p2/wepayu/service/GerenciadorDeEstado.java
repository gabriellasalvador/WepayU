package br.ufal.ic.p2.wepayu.service;

import br.ufal.ic.p2.wepayu.excecoes.sistema.NaoHaComandoDesfazerException;
import br.ufal.ic.p2.wepayu.excecoes.sistema.NaoHaComandoRefazerException;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.models.agenda.CatalogoDeAgendas;
import br.ufal.ic.p2.wepayu.repository.RepositorioEmpregados;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Cuida do historico de comandos (undo/redo) com o padrao Memento.
 *
 * Papeis do padrao: esta classe cria as fotos do estado (EstadoDoSistema) e sabe
 * restaura-las, e tambem guarda as fotos nas pilhas de desfazer e refazer. O estado
 * do sistema que entra em cada foto e formado pelo repositorio de empregados (com o
 * proximo id) pelas folhas ja geradas (FolhaDePagamento) e pelas agendas de pagamento
 * disponiveis (CatalogoDeAgendas).
 *
 * Cada comando que altera dados tira uma foto ANTES de executar e a registra com
 * registrarComando. O mesmo mecanismo serve para gravar o estado em disco ao encerrar.
 */
public class GerenciadorDeEstado {
    private final RepositorioEmpregados repositorio;
    private final FolhaDePagamento folha;
    private final CatalogoDeAgendas catalogo;

    // Cada comando que muda o estado empilha em pilhaUndo a foto de ANTES de executar.
    private final Deque<EstadoDoSistema> pilhaUndo = new ArrayDeque<>();
    private final Deque<EstadoDoSistema> pilhaRedo = new ArrayDeque<>();

    public GerenciadorDeEstado(RepositorioEmpregados repositorio, FolhaDePagamento folha, CatalogoDeAgendas catalogo) {
        this.repositorio = repositorio;
        this.folha = folha;
        this.catalogo = catalogo;
    }

    /**
     * Serializa uma foto completa do estado mutavel do sistema: os empregados e o
     * proximo id (vindos do repositorio) e as folhas ja geradas.
     */
    public EstadoDoSistema tirarFoto() {
        ByteArrayOutputStream bufferDeBytes = new ByteArrayOutputStream();
        // try-with-resources: o stream e fechado (e descarregado) mesmo se algo falhar no meio.
        try (ObjectOutputStream escritorDeObjetos = new ObjectOutputStream(bufferDeBytes)) {
            escritorDeObjetos.writeObject(repositorio.copiarEmpregados());
            escritorDeObjetos.writeObject(folha.copiarFolhasGeradas());
            escritorDeObjetos.writeInt(repositorio.getProximoId());
            escritorDeObjetos.writeObject(catalogo.copiarAgendas());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao tirar foto do sistema: " + e.getMessage(), e);
        }
        return new EstadoDoSistema(bufferDeBytes.toByteArray());
    }

    /**
     * Substitui o estado atual (repositorio e folhas geradas) pelo que foi guardado
     * na foto. Usado por undo, redo e na restauracao do estado ao iniciar o sistema.
     */
    @SuppressWarnings("unchecked")
    public void restaurar(EstadoDoSistema foto) {
        try (ObjectInputStream leitorDeObjetos = new ObjectInputStream(new ByteArrayInputStream(foto.getDados()))) {
            Map<String, Empregado> empregadosRestaurados = (Map<String, Empregado>) leitorDeObjetos.readObject();
            Map<LocalDate, String> folhasRestauradas = (Map<LocalDate, String>) leitorDeObjetos.readObject();
            int proximoIdRestaurado = leitorDeObjetos.readInt();
            Map<String, AgendaPagamento> agendasRestauradas = (Map<String, AgendaPagamento>) leitorDeObjetos.readObject();
            // Troca so o conteudo do repositorio unico; o objeto repositorio continua o mesmo.
            repositorio.restaurar(empregadosRestaurados, proximoIdRestaurado);
            folha.restaurarFolhasGeradas(folhasRestauradas);
            catalogo.restaurar(agendasRestauradas);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erro ao restaurar foto do sistema: " + e.getMessage(), e);
        }
    }

    /**
     * Empilha a foto do estado anterior a execucao de um comando que muda dados,
     * habilitando o undo. Qualquer novo comando invalida a pilha de redo (nao faz
     * mais sentido refazer algo depois que um novo comando foi executado).
     */
    public void registrarComando(EstadoDoSistema fotoAntes) {
        pilhaUndo.push(fotoAntes);
        pilhaRedo.clear();
    }

    /**
     * Desfaz o ultimo comando que alterou o estado: guarda o estado atual na pilha
     * de redo e restaura o estado anterior guardado na pilha de undo.
     */
    public void desfazer() throws NaoHaComandoDesfazerException {
        if (pilhaUndo.isEmpty()) throw new NaoHaComandoDesfazerException();

        EstadoDoSistema estadoAtual = tirarFoto();
        EstadoDoSistema estadoAnterior = pilhaUndo.pop();
        pilhaRedo.push(estadoAtual);
        restaurar(estadoAnterior);
    }

    /**
     * Refaz o ultimo comando desfeito: guarda o estado atual na pilha de undo e
     * restaura o estado seguinte guardado na pilha de redo.
     */
    public void refazer() throws NaoHaComandoRefazerException {
        if (pilhaRedo.isEmpty()) throw new NaoHaComandoRefazerException();

        EstadoDoSistema estadoAtual = tirarFoto();
        EstadoDoSistema estadoSeguinte = pilhaRedo.pop();
        pilhaUndo.push(estadoAtual);
        restaurar(estadoSeguinte);
    }
}