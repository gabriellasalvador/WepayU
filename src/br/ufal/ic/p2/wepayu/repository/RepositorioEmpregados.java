package br.ufal.ic.p2.wepayu.repository;

import br.ufal.ic.p2.wepayu.excecoes.sindicato.IdentificacaoSindicatoDuplicadaException;
import br.ufal.ic.p2.wepayu.excecoes.sindicato.TaxaSindicalNegativaException;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.sindicato.Filiado;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    /** Id que o proximo empregado cadastrado vai receber (como texto); nao avanca o contador. */
    public String proximoId() {
        return String.valueOf(proximoId);
    }

    /**
     * Cadastra um empregado novo e avanca o contador de ids. O empregado deve ter sido
     * criado com o id de proximoId(); assim, se os dados dele forem invalidos e a criacao
     * falhar, nenhum id e gasto.
     */
    public void cadastrar(Empregado novo) {
        empregados.put(novo.getId(), novo);
        proximoId++;
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

    /**
     * Empregados cujo nome contem o trecho informado, ordenados pelo id (numerico).
     * A lista devolvida e uma copia: alterar a lista nao altera o repositorio.
     */
    public List<Empregado> buscarPorNome(String trecho) {
        List<Empregado> encontrados = new ArrayList<>();
        for (Empregado empregado : empregados.values()) {
            if (empregado.getNome().contains(trecho)) {
                encontrados.add(empregado);
            }
        }
        encontrados.sort(Comparator.comparing(e -> Integer.parseInt(e.getId())));
        return encontrados;
    }

    /** Procura o empregado sindicalizado que tem o id de sindicato informado. */
    public Optional<Empregado> buscarPorIdSindicato(String idSindicato) {
        for (Empregado empregado : empregados.values()) {
            if (empregado.getSindicato().temIdSindicato(idSindicato)) {
                return Optional.of(empregado);
            }
        }
        return Optional.empty();
    }

    /**
     * Filia o empregado ao sindicato com o id e a taxa informados. O id de sindicato
     * precisa ser unico entre os empregados; se ja pertencer a OUTRO empregado, lanca
     * IdentificacaoSindicatoDuplicadaException (o proprio empregado pode reaplicar o
     * seu id). Como o repositorio enxerga todos os empregados, a regra mora aqui.
     */
    public void filiar(Empregado empregado, String idSindicato, double taxaSindical)
            throws IdentificacaoSindicatoDuplicadaException, TaxaSindicalNegativaException {
        if (idSindicatoEmUso(idSindicato, empregado)) throw new IdentificacaoSindicatoDuplicadaException();
        empregado.setSindicato(new Filiado(idSindicato, taxaSindical));
    }

    /** Informa se o id de sindicato ja pertence a OUTRO empregado sindicalizado. */
    private boolean idSindicatoEmUso(String idSindicato, Empregado exceto) {
        for (Empregado outro : empregados.values()) {
            if (outro != exceto && outro.getSindicato().temIdSindicato(idSindicato)) {
                return true;
            }
        }
        return false;
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