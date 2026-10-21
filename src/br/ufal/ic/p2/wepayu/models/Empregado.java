package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.tipoempregado.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.Exception.tipoempregado.EmpregadoNaoEhHoristaException;
import br.ufal.ic.p2.wepayu.models.pagamento.EmMaos;
import br.ufal.ic.p2.wepayu.models.pagamento.MetodoPagamento;
import java.util.ArrayList;
import java.util.Collections;
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
    // Flag simples de sindicalizacao; os demais dados do sindicato (idSindicato,
    // taxaSindical, divida, taxas de servico) so tem sentido quando isso e true.
    private boolean sindicalizado;

    /**
     * Novo empregado sempre comeca nao sindicalizado; os demais dados de sindicato
     * so sao preenchidos depois, via sindicalizar(...).
     */
    public Empregado(String id, String nome, String endereco, double salario) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.salario = salario;
        this.sindicalizado = false;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public double getSalario() { return salario; }
    public boolean isSindicalizado() { return sindicalizado; }

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

    // ---------- Dados de sindicalizacao (comuns a qualquer tipo de empregado) ----------
    private String idSindicato;
    private double taxaSindical;
    // Saldo devedor acumulado de taxa sindical/taxas de servico ainda nao descontado
    // em folha; e atualizado pela Facade a cada folha rodada (calcularDescontoSindicato).
    private double dividaSindical = 0;
    private List<TaxaServico> taxasServico = new ArrayList<>();

    public String getIdSindicato() { return idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }


    public double getDividaSindical() { return dividaSindical; }
    public void setDividaSindical(double dividaSindical) {
        this.dividaSindical = dividaSindical;
    }


    /**
     * Associa o empregado a um sindicato, definindo id e taxa sindical fixa.
     */
    public void sindicalizar(String idSindicato, double taxaSindical) {
        this.sindicalizado = true;
        this.idSindicato = idSindicato;
        this.taxaSindical = taxaSindical;
    }

    /**
     * Remove a sindicalizacao do empregado, limpando id e taxa sindical.
     * Note que a divida sindical acumulada (dividaSindical) e as taxas de servico
     * ja lancadas nao sao apagadas aqui.
     */
    public void dessindicalizar() {
        this.sindicalizado = false;
        this.idSindicato = null;
        this.taxaSindical = 0;
    }

    public void addTaxaServico(TaxaServico taxa) {
        taxasServico.add(taxa);
    }

    /**
     * Devolve uma visao somente leitura das taxas de servico do empregado.
     * A lista interna nao e exposta: qualquer alteracao precisa passar por
     * addTaxaServico, o que garante que o estado so muda por comandos da Facade
     * (e, portanto, que cada mudanca entra no undo/redo).
     */
    public List<TaxaServico> getTaxasServico() {
        return Collections.unmodifiableList(taxasServico);
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
        this.sindicalizado = outro.sindicalizado;
        this.idSindicato = outro.idSindicato;
        this.taxaSindical = outro.taxaSindical;
        // Copia defensiva: o novo empregado nao pode compartilhar a mesma lista com o antigo.
        this.taxasServico = new ArrayList<>(outro.taxasServico);
        // Pode compartilhar a referencia: as formas de pagamento sao imutaveis.
        this.metodoPagamento = outro.metodoPagamento;
        this.dividaSindical = outro.dividaSindical;

    }




}