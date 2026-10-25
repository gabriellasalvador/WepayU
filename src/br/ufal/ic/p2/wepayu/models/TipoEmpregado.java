package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.cadastro.TipoInvalidoException;

/**
 * Os tipos de empregado que o sistema conhece, e tambem a FABRICA que cria cada um
 * (padrao Factory). Cada constante sabe o seu nome de texto (o mesmo usado nos
 * comandos e testes), quais dados extras o seu cadastro/troca aceita e como
 * instanciar a classe correspondente. Com isso a Facade nao compara o tipo por
 * texto nem escolhe a classe com if/else: pede a fabrica.
 *
 * Para criar um quarto tipo de empregado basta escrever a nova classe (subclasse
 * de Empregado) e acrescentar UMA constante aqui; criarEmpregado, alteraEmpregado
 * e o relatorio da folha nao precisam ser alterados.
 */
public enum TipoEmpregado {

    HORISTA("horista", false, true) {
        @Override
        public Empregado criar(String id, String nome, String endereco, double salario, double comissao) {
            return new EmpregadoHorista(id, nome, endereco, salario);
        }
    },

    ASSALARIADO("assalariado", false, false) {
        @Override
        public Empregado criar(String id, String nome, String endereco, double salario, double comissao) {
            return new EmpregadoAssalariado(id, nome, endereco, salario);
        }
    },

    COMISSIONADO("comissionado", true, false) {
        @Override
        public Empregado criar(String id, String nome, String endereco, double salario, double comissao) {
            return new EmpregadoComissionado(id, nome, endereco, salario, comissao);
        }
    };

    private final String nome;
    private final boolean exigeComissao;
    private final boolean trocaComNovoSalario;

    TipoEmpregado(String nome, boolean exigeComissao, boolean trocaComNovoSalario) {
        this.nome = nome;
        this.exigeComissao = exigeComissao;
        this.trocaComNovoSalario = trocaComNovoSalario;
    }

    /**
     * Cria um empregado deste tipo. O parametro "comissao" so e usado pelos tipos
     * que a exigem (exigeComissao()); os demais o ignoram.
     */
    public abstract Empregado criar(String id, String nome, String endereco, double salario, double comissao);

    /**
     * Converte um empregado para este tipo: cria pela fabrica uma nova instancia (mantendo
     * id, nome e endereco) com o salario e a comissao informados e copia para ela o estado
     * comum do empregado antigo (sindicalizacao e forma de pagamento). O empregado antigo
     * nao e alterado; quem chama e responsavel por guardar o novo no lugar dele.
     */
    public Empregado converter(Empregado antigo, double novoSalario, double novaComissao) {
        Empregado novo = criar(antigo.getId(), antigo.getNome(), antigo.getEndereco(), novoSalario, novaComissao);
        novo.copiarEstadoComumDe(antigo);
        return novo;
    }

    /** Nome do tipo como aparece nos comandos e no atributo "tipo" ("horista", "assalariado", "comissionado"). */
    public String getNome() {
        return nome;
    }

    /** Informa se o cadastro deste tipo exige o valor da comissao (so o comissionado). */
    public boolean exigeComissao() {
        return exigeComissao;
    }

    /**
     * Informa se, ao trocar um empregado para este tipo, o comando pode trazer junto o
     * novo salario (so o horista, que informa o valor da hora). O comissionado traz a
     * comissao, e o assalariado nao traz dado extra.
     */
    public boolean trocaComNovoSalario() {
        return trocaComNovoSalario;
    }

    /**
     * Procura o tipo pelo nome de texto.
     *
     * @throws TipoInvalidoException se o nome for nulo ou nao corresponder a nenhum tipo
     */
    public static TipoEmpregado porNome(String nome) throws TipoInvalidoException {
        for (TipoEmpregado tipo : values()) {
            if (tipo.nome.equals(nome)) return tipo;
        }
        throw new TipoInvalidoException();
    }
}