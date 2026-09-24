package br.ufal.ic.p2.wepayu.models;
 
import br.ufal.ic.p2.wepayu.Exception.tipoempregado.EmpregadoNaoEhComissionadoException;
import br.ufal.ic.p2.wepayu.Exception.tipoempregado.EmpregadoNaoEhHoristaException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
 
/**
 * Classe base abstrata para todos os tipos de empregado (horista, assalariado,
 * comissionado). Concentra o estado e o comportamento comuns a qualquer empregado
 * (identificacao, endereco, salario, sindicalizacao, forma de pagamento) e deixa
 * para as subclasses apenas o que realmente varia por tipo (getTipo(), e o
 * comportamento de cartao de ponto/vendas, que por padrao lanca excecao aqui
 * e e sobrescrito só pelo tipo que faz sentido).
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
 
    public List<TaxaServico> getTaxasServico() {
        return taxasServico;
    }
 
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setSalario(double salario) { this.salario = salario; }
 
    // ---------- Forma de pagamento (comum a qualquer tipo de empregado) ----------
    // Por padrao, todo empregado novo recebe pagamento em maos.
    private String metodoPagamento = "emMaos";
    private String banco;
    private String agencia;
    private String contaCorrente;
 
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }
 
    /**
     * Muda a forma de pagamento para "em maos", limpando quaisquer dados bancarios
     * que tenham sido definidos anteriormente.
     */
    public void definirPagamentoEmMaos() {
        metodoPagamento = "emMaos";
        banco = null; agencia = null; contaCorrente = null;
    }
 
    /**
     * Muda a forma de pagamento para "correios", limpando quaisquer dados bancarios
     * que tenham sido definidos anteriormente.
     */
    public void definirPagamentoCorreios() {
        metodoPagamento = "correios";
        banco = null; agencia = null; contaCorrente = null;
    }
 
    /**
     * Muda a forma de pagamento para "banco", registrando os dados bancarios informados.
     */
    public void definirPagamentoBanco(String banco, String agencia, String contaCorrente) {
        metodoPagamento = "banco";
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
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
        this.taxasServico = outro.taxasServico;
        this.metodoPagamento = outro.metodoPagamento;
        this.banco = outro.banco;
        this.agencia = outro.agencia;
        this.contaCorrente = outro.contaCorrente;
        this.dividaSindical = outro.dividaSindical;
 
    }
 
 
 
 
}
 


