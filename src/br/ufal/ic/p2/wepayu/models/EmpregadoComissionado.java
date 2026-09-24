package br.ufal.ic.p2.wepayu.models;
 
import java.util.ArrayList;
import java.util.List;
 
/**
 * Empregado comissionado: recebe um salario fixo quinzenal (proporcional ao salario
 * mensal informado) mais uma comissao sobre o total de vendas realizadas no periodo,
 * pago a cada 14 dias a partir de 14/01/2005 (ver Facade.isDiaPagamentoComissionado).
 * Diferente da superclasse, aqui getComissao/setComissao e addVenda/getVendas
 * realmente funcionam em vez de lancar excecao, pois so faz sentido para esse tipo.
 */
public class EmpregadoComissionado extends Empregado {
    // Percentual (fracao) de comissao aplicado sobre o total de vendas do periodo.
    private double comissao;
    // Historico de todas as vendas lancadas para este empregado.
    private List<ResultadoVenda> vendas = new ArrayList<>();
 
    public EmpregadoComissionado(String id, String nome, String endereco, double salario, double comissao) {
        super(id, nome, endereco, salario);
        this.comissao = comissao;
    }
 
    @Override
    public String getTipo() { return "comissionado"; }
 
    @Override
    public double getComissao() { return comissao; }
 
    @Override
    public void setComissao(double comissao) {
        this.comissao = comissao;
    }
 
    @Override
    public void addVenda(ResultadoVenda venda) {
        vendas.add(venda);
    }
 
    @Override
    public List<ResultadoVenda> getVendas() {
        return vendas;
    }
}
 


