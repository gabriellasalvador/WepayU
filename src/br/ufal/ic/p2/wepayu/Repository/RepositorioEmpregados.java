package br.ufal.ic.p2.wepayu.repository;

import br.ufal.ic.p2.wepayu.models.Empregado;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de empregados (padrao Repository): e o unico lugar do sistema que
 * guarda os empregados cadastrados e o contador usado para gerar os proximos ids.
 * Quem precisa de empregados (a Facade) pede a este repositorio; ninguem mexe no
 * mapa interno diretamente.
 *
 * Tambem e um Singleton: existe uma unica instancia em toda a aplicacao, obtida por
 * getInstancia(); o construtor e privado, entao nao ha como criar um segundo
 * repositorio com empregados diferentes. A instancia e criada junto com a classe
 * (inicializacao imediata), o que ja a torna segura entre threads.
 *
 * O estado pode ser reiniciado (zerarSistema, undo, redo e leitura do estado.dat) pelos
 * metodos zerar e restaurar, que trocam o conteudo do repositorio. A instancia em si
 * nunca muda, entao todos que a usam continuam enxergando o mesmo repositorio.
 */
public final class RepositorioEmpregados {
    private static final RepositorioEmpregados INSTANCIA = new RepositorioEmpregados();

    // Mapa id do empregado -> objeto Empregado.
    private Map<String, Empregado> empregados = new HashMap<>();
    // Contador usado para gerar os proximos ids de empregado (1, 2, 3, ...).
    private int proximoId = 1;

    private RepositorioEmpregados() {
        // Singleton: so a propria classe cria a instancia.
    }

    /** Devolve a unica instancia do repositorio. */
    public static RepositorioEmpregados getInstancia() {
        return INSTANCIA;
    }

    /** Gera o proximo id de empregado (como texto) e avanca o contador. */
    public String gerarProximoId() {
        return String.valueOf(proximoId++);
    }

    /**
     * Guarda o empregado, usando o id dele como chave. Se ja existir um empregado
     * com esse id (por exemplo, na troca de tipo), ele e substituido.
     */
    public void salvar(Empregado empregado) {
        empregados.put(empregado.getId(), empregado);
    }

    /** Procura um empregado pelo id; vazio se nao existir. */
    public Optional<Empregado> buscarPorId(String id) {
        return Optional.ofNullable(empregados.get(id));
    }

    /** Informa se existe um empregado com o id. */
    public boolean existe(String id) {
        return empregados.containsKey(id);
    }

    /** Remove o empregado com o id informado (nada acontece se nao existir). */
    public void remover(String id) {
        empregados.remove(id);
    }

    /**
     * Todos os empregados cadastrados, como visao somente leitura: quem consulta nao
     * consegue incluir nem remover empregados por fora do repositorio.
     */
    public Collection<Empregado> listar() {
        return Collections.unmodifiableCollection(empregados.values());
    }

    /** Quantidade de empregados cadastrados. */
    public int quantidade() {
        return empregados.size();
    }

    /** Apaga todos os empregados e volta o contador de ids para 1. */
    public void zerar() {
        empregados.clear();
        proximoId = 1;
    }

    // ---------- Apoio ao undo/redo e a persistencia (fotos do estado) ----------

    /** Copia dos empregados para ser gravada numa "foto" do estado do sistema. */
    public Map<String, Empregado> copiarEmpregados() {
        return new HashMap<>(empregados);
    }

    /** Proximo id que sera gerado; tambem entra na "foto" do estado. */
    public int getProximoId() {
        return proximoId;
    }

    /**
     * Substitui todo o conteudo do repositorio pelo de uma "foto" restaurada. O
     * repositorio continua sendo o mesmo objeto; so os dados dentro dele mudam.
     */
    public void restaurar(Map<String, Empregado> empregadosRestaurados, int proximoIdRestaurado) {
        this.empregados = empregadosRestaurados;
        this.proximoId = proximoIdRestaurado;
    }
}