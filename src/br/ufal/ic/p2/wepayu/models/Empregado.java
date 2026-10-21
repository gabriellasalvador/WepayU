package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.tipoempregado.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.Exception.tipoempregado.EmpregadoNaoEhHoristaException;
import br.ufal.ic.p2.wepayu.models.sindicato.FiliacaoSindical;
import br.ufal.ic.p2.wepayu.models.sindicato.NaoFiliado;
import br.ufal.ic.p2.wepayu.models.pagamento.EmMaos;
import br.ufal.ic.p2.wepayu.models.pagamento.MetodoPagamento;
import java.util.List;
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

    /**
     * Novo empregado sempre comeca nao sindicalizado (NaoFiliado); a filiacao
     * so muda depois, via setSindicato(new Filiado(...)).
     */
    public Empregado(String id, String nome, String endereco, double salario) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.salario = salario;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public double getSalario() { return salario; }

    /**
     * Cada subclasse concreta informa seu proprio tipo ("horista", "assalariado"
     * ou "comissionado"), usado pela Facade para decidir regras de calculo e
     * validacao especificas.
     */
    public abstract String getTipo();

    /**
     * Lancamento de cartao de ponto so faz sentido para horistas; a implementacao
     * padrao aqui lanca excecao e e sobrescrita apenas em EmpregadoHorista.
     */
    public void addCartao(CartaoDePonto cartao) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    /**
     * Idem addCartao: obter a lista de cartoes so e valido para horistas.
     */
    public List<CartaoDePonto> getCartoes() throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    /**
     * Comissao so existe para empregados comissionados; comportamento padrao
     * (excecao) e sobrescrito em EmpregadoComissionado.
     */
    public double getComissao() throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public void setComissao(double comissao) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    /**
     * Lancamento de vendas so faz sentido para comissionados; comportamento padrao
     * (excecao) e sobrescrito em EmpregadoComissionado.
     */
    public void addVenda(ResultadoVenda venda) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public List<ResultadoVenda> getVendas() throws EmpregadoNaoEhComissionadoException {
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
    public void setSalario(double salario) { this.salario = salario; }

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
    public void copiarEstadoComumDe(Empregado outro) {
        // Copia independente: o novo empregado nao pode compartilhar a filiacao
        // (divida e lista de taxas) com o antigo.
        this.sindicato = outro.sindicato.copiar();
        // Pode compartilhar a referencia: as formas de pagamento sao imutaveis.
        this.metodoPagamento = outro.metodoPagamento;

    }




}