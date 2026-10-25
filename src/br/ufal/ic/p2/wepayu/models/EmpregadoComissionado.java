package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.cadastro.ComissaoNegativaException;
import br.ufal.ic.p2.wepayu.excecoes.cadastro.SalarioNegativoException;

import br.ufal.ic.p2.wepayu.models.agenda.AgendaSemanal;
import br.ufal.ic.p2.wepayu.models.folha.CategoriaFolha;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Empregado comissionado: recebe uma parte fixa do salario mensal informado mais uma
 * comissao sobre o total de vendas realizadas no periodo; por padrao e pago a cada
 * 14 dias, em sextas-feiras (agenda "semanal 2 5", a partir de 14/01/2005).
 * Diferente da superclasse, aqui getComissao/setComissao e addVenda/getVendas
 * realmente funcionam em vez de lancar excecao, pois so faz sentido para esse tipo.
 */
public class EmpregadoComissionado extends Empregado {
    // Percentual (fracao) de comissao aplicado sobre o total de vendas do periodo.
    private double comissao;
    // Historico de todas as vendas lancadas para este empregado.
    private List<ResultadoVenda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco, double salario, double comissao)
            throws SalarioNegativoException, ComissaoNegativaException {
        super(id, nome, endereco, salario, new AgendaSemanal(2, DayOfWeek.FRIDAY));
        if (comissao < 0) throw new ComissaoNegativaException();
        this.comissao = comissao;
    }

    @Override
    public TipoEmpregado getTipo() { return TipoEmpregado.COMISSIONADO; }

    @Override
    public CategoriaFolha getCategoria() {
        return CategoriaFolha.COMISSIONADOS;
    }

    /**
     * Valores do comissionado no periodo: parte fixa do salario, total de vendas,
     * comissao sobre essas vendas e salario bruto (fixo + comissao). Considera so as
     * vendas dentro do periodo.
     */
    @Override
    protected double[] calcularParcelas(LocalDate inicioExclusive, LocalDate fimInclusive) {
        double vendasPeriodo = 0;
        for (ResultadoVenda v : vendas) {
            LocalDate d = v.getData();
            if (d.isAfter(inicioExclusive) && !d.isAfter(fimInclusive)) vendasPeriodo += v.getValor();
        }
        double fixo = Dinheiro.truncar(getAgenda().parteFixaDoSalario(getSalario()));
        double comissaoValor = Dinheiro.truncar(vendasPeriodo * comissao);
        double bruto = Dinheiro.truncar(fixo + comissaoValor);
        return new double[] { fixo, vendasPeriodo, comissaoValor, bruto };
    }

    @Override
    public double getComissao() { return comissao; }

    @Override
    public void setComissao(double comissao) throws ComissaoNegativaException {
        if (comissao < 0) throw new ComissaoNegativaException();
        this.comissao = comissao;
    }

    @Override
    public void addVenda(ResultadoVenda venda) {
        vendas.add(venda);
    }

    /** Soma do valor das vendas realizadas dentro do periodo [inicio, fim). */
    @Override
    public double totalVendasEm(Periodo periodo) {
        double total = 0;
        for (ResultadoVenda venda : vendas) {
            if (periodo.contem(venda.getData())) total += venda.getValor();
        }
        return total;
    }
}