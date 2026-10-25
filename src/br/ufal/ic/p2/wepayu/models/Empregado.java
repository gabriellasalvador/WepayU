package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.cadastro.ComissaoNegativaException;
import br.ufal.ic.p2.wepayu.excecoes.cadastro.SalarioNegativoException;

import br.ufal.ic.p2.wepayu.excecoes.tipoempregado.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.excecoes.tipoempregado.EmpregadoNaoEhHoristaException;
import br.ufal.ic.p2.wepayu.models.agenda.AgendaPagamento;
import br.ufal.ic.p2.wepayu.models.folha.CategoriaFolha;
import br.ufal.ic.p2.wepayu.models.folha.LinhaFolha;
import br.ufal.ic.p2.wepayu.models.sindicato.FiliacaoSindical;
import br.ufal.ic.p2.wepayu.models.sindicato.NaoFiliado;
import br.ufal.ic.p2.wepayu.models.pagamento.EmMaos;
import br.ufal.ic.p2.wepayu.models.pagamento.MetodoPagamento;
import java.time.LocalDate;
import java.util.Objects;
import java.io.Serializable;

/**
 * Classe base abstrata para todos os tipos de empregado (horista, assalariado,
 * comissionado). Concentra o estado e o comportamento comuns a qualquer empregado
 * (identificacao, endereco, salario, sindicalizacao, forma de pagamento) e deixa
 * para as subclasses apenas o que realmente varia por tipo (getTipo(), e o
 * comportamento de cartao de ponto/vendas, que por padrao lanca excecao aqui
 * e e sobrescrito so pelo tipo que faz sentido).
 *
 * Implementa Serializable porque a Facade usa serializacao (ObjectOutputStream)
 * para tirar "fotos" do estado do sistema no mecanismo de undo/redo/persistencia.
 */
public abstract class Empregado implements Serializable{
    // Dados basicos de identificacao do empregado.
    private String id;
    private String nome;
    private String endereco;
    private double salario;
    // Strategy: define quando e pago e qual periodo cada pagamento cobre.
    private AgendaPagamento agenda;

    /**
     * Novo empregado sempre comeca nao sindicalizado (NaoFiliado); a filiacao
     * so muda depois, via setSindicato(new Filiado(...)).
     *
     * @param agenda agenda de pagamento do empregado; cada tipo informa a sua padrao
     *               (horista: semanal 5, assalariado: mensal $, comissionado: semanal 2 5)
     */
    public Empregado(String id, String nome, String endereco, double salario, AgendaPagamento agenda)
            throws SalarioNegativoException {
        if (salario < 0) throw new SalarioNegativoException();
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.salario = salario;
        this.agenda = agenda;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public double getSalario() { return salario; }

    /** Agenda de pagamento do empregado (quando e pago e qual periodo cada pagamento cobre). */
    public AgendaPagamento getAgenda() { return agenda; }

    /** Troca a agenda de pagamento do empregado (por uma que exista no CatalogoDeAgendas). */
    public void setAgenda(AgendaPagamento agenda) { this.agenda = agenda; }

    /**
     * Cada subclasse concreta informa o seu proprio tipo (TipoEmpregado.HORISTA,
     * ASSALARIADO ou COMISSIONADO).
     */
    public abstract TipoEmpregado getTipo();

    /**
     * Informa em qual secao do relatorio da folha este empregado aparece. Cada tipo
     * responde pelo seu proprio caso, entao a Facade nao precisa comparar o tipo.
     */
    public abstract CategoriaFolha getCategoria();

    /**
     * Calcula os valores especificos do tipo para o periodo (inicioExclusive, fimInclusive],
     * na ordem das colunas da sua CategoriaFolha, sem os descontos nem o liquido. O ULTIMO
     * valor devolvido deve ser sempre o salario bruto. E aqui que cada tipo de empregado
     * aplica a sua propria regra de salario (horas, parte fixa, comissao etc.).
     */
    protected abstract double[] calcularParcelas(LocalDate inicioExclusive, LocalDate fimInclusive);

    /**
     * Calcula o pagamento deste empregado na data informada. Metodo-modelo (template
     * method): o periodo vem da agenda, os valores do tipo vem de calcularParcelas
     * (implementado em cada subclasse) e o desconto vem da situacao no sindicato.
     *
     * @param aplicarMutacao true grava o novo saldo de divida sindical (rodaFolha);
     *                       false apenas simula (totalFolha, que e so uma previa)
     */
    public final LinhaFolha calcularLinhaFolha(LocalDate data, boolean aplicarMutacao) {
        LocalDate inicio = agenda.inicioDoPeriodo(data);
        double[] parcelas = calcularParcelas(inicio, data);
        double bruto = parcelas[parcelas.length - 1];
        double descontos = sindicato.calcularDesconto(inicio, data, agenda.diasNoPeriodo(data), bruto, aplicarMutacao);
        return new LinhaFolha(parcelas, descontos);
    }

    /**
     * Lancamento de cartao de ponto so faz sentido para horistas; a implementacao
     * padrao aqui lanca excecao e e sobrescrita apenas em EmpregadoHorista.
     */
    public void addCartao(CartaoDePonto cartao) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    /**
     * Total de horas normais trabalhadas dentro do periodo. Como o cartao de ponto, so
     * faz sentido para horistas; aqui lanca excecao e EmpregadoHorista sobrescreve.
     */
    public double horasNormaisEm(Periodo periodo) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    /**
     * Total de horas extras trabalhadas dentro do periodo (so para horistas).
     */
    public double horasExtrasEm(Periodo periodo) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    /**
     * Comissao so existe para empregados comissionados; comportamento padrao
     * (excecao) e sobrescrito em EmpregadoComissionado.
     */
    public double getComissao() throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public void setComissao(double comissao) throws EmpregadoNaoEhComissionadoException, ComissaoNegativaException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    /**
     * Lancamento de vendas so faz sentido para comissionados; comportamento padrao
     * (excecao) e sobrescrito em EmpregadoComissionado.
     */
    public void addVenda(ResultadoVenda venda) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    /**
     * Valor total das vendas realizadas dentro do periodo. So existe para
     * comissionados; EmpregadoComissionado sobrescreve este metodo.
     */
    public double totalVendasEm(Periodo periodo) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    // ---------- Sindicato (comum a qualquer tipo de empregado) ----------
    // State/Null Object: a situacao perante o sindicato e um objeto (Filiado ou
    // NaoFiliado) que guarda os dados e as regras de sindicato (taxa sindical,
    // taxas de servico, divida e desconto da folha). Todo empregado novo comeca
    // como NaoFiliado.
    private FiliacaoSindical sindicato = new NaoFiliado();

    /**
     * Devolve a situacao do empregado perante o sindicato. Nunca e nula: para quem
     * nao e filiado devolve um NaoFiliado (use isFiliado() para saber).
     */
    public FiliacaoSindical getSindicato() { return sindicato; }

    /**
     * Troca a situacao perante o sindicato: new Filiado(id, taxa) para filiar,
     * new NaoFiliado() para desfiliar. Os dados da situacao anterior (divida e
     * taxas de servico) somem junto com o objeto antigo.
     */
    public void setSindicato(FiliacaoSindical sindicato) {
        this.sindicato = Objects.requireNonNull(sindicato, "sindicato");
    }

    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    /** @throws SalarioNegativoException se o salario for negativo */
    public void setSalario(double salario) throws SalarioNegativoException {
        if (salario < 0) throw new SalarioNegativoException();
        this.salario = salario;
    }

    // ---------- Forma de pagamento (comum a qualquer tipo de empregado) ----------
    // Strategy: a forma de pagamento e um objeto (EmMaos, Correios ou Banco) que
    // sabe se descrever e, no caso do banco, guarda seus proprios dados.
    // Por padrao, todo empregado novo recebe pagamento em maos.
    private MetodoPagamento metodoPagamento = new EmMaos();

    /**
     * Devolve a forma de pagamento atual do empregado. Como as formas de
     * pagamento sao imutaveis, o objeto pode ser devolvido sem copia.
     */
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }

    /**
     * Troca a forma de pagamento do empregado. Os dados da forma anterior (por
     * exemplo, a conta bancaria) somem junto com o objeto antigo, sem precisar
     * de nenhuma limpeza manual.
     */
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = Objects.requireNonNull(metodoPagamento, "metodoPagamento");
    }

    /**
     * Copia para este empregado todo o estado que independe do tipo (sindicalizacao,
     * divida sindical, taxas de servico lancadas e forma de pagamento) a partir de
     * "outro". Usado pela Facade quando o tipo de um empregado e trocado (trocarTipo):
     * uma nova instancia da subclasse correta e criada, e esse metodo garante que ela
     * nao perca os dados comuns que o empregado antigo ja tinha.
     */
    // Obs.: a agenda NAO e copiada; o novo tipo comeca com a agenda padrao do seu tipo.
    public void copiarEstadoComumDe(Empregado outro) {
        // Copia independente: o novo empregado nao pode compartilhar a filiacao
        // (divida e lista de taxas) com o antigo.
        this.sindicato = outro.sindicato.copiar();
        // Pode compartilhar a referencia: as formas de pagamento sao imutaveis.
        this.metodoPagamento = outro.metodoPagamento;

    }




}