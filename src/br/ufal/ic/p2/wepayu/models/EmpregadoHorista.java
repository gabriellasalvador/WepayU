package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.cadastro.SalarioNegativoException;

import br.ufal.ic.p2.wepayu.models.agenda.AgendaSemanal;
import br.ufal.ic.p2.wepayu.models.folha.CategoriaFolha;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Empregado horista: recebe por hora trabalhada, registrada via cartoes de ponto,
 * com pagamento semanal por padrao (toda sexta-feira, agenda "semanal 5").
 * Horas ate 8 por cartao contam como normais e o excedente como extra (pago a 1.5x).
 * Diferente da superclasse, aqui addCartao/getCartoes realmente funcionam em vez
 * de lancar excecao, pois so faz sentido para esse tipo.
 */
public class EmpregadoHorista extends Empregado {
    // Historico de todos os cartoes de ponto (data + horas) lancados para este empregado.
    private List<CartaoDePonto> cartoes = new ArrayList<>();

    public EmpregadoHorista(String id, String nome, String endereco, double salario) throws SalarioNegativoException {
        super(id, nome, endereco, salario, new AgendaSemanal(1, DayOfWeek.FRIDAY));
    }

    @Override
    public TipoEmpregado getTipo() { return TipoEmpregado.HORISTA; }

    @Override
    public CategoriaFolha getCategoria() {
        return CategoriaFolha.HORISTAS;
    }

    /**
     * Valores do horista no periodo: horas normais, horas extras e salario bruto.
     * Considera so os cartoes de ponto dentro do periodo; ate 8 horas por cartao
     * sao normais e o excedente e extra (pago a 1.5x o valor da hora).
     */
    @Override
    protected double[] calcularParcelas(LocalDate inicioExclusive, LocalDate fimInclusive) {
        double normais = 0, extras = 0;
        for (CartaoDePonto c : cartoes) {
            LocalDate d = c.getData();
            if (d.isAfter(inicioExclusive) && !d.isAfter(fimInclusive)) {
                normais += c.getHorasNormais();
                extras += c.getHorasExtras();
            }
        }
        double bruto = Dinheiro.truncar(normais * getSalario() + extras * getSalario() * 1.5);
        return new double[] { normais, extras, bruto };
    }

    @Override
    public void addCartao(CartaoDePonto cartao) {
        cartoes.add(cartao);
    }

    /** Soma das horas normais dos cartoes lancados dentro do periodo [inicio, fim). */
    @Override
    public double horasNormaisEm(Periodo periodo) {
        double total = 0;
        for (CartaoDePonto cartao : cartoes) {
            if (periodo.contem(cartao.getData())) total += cartao.getHorasNormais();
        }
        return total;
    }

    /** Soma das horas extras dos cartoes lancados dentro do periodo [inicio, fim). */
    @Override
    public double horasExtrasEm(Periodo periodo) {
        double total = 0;
        for (CartaoDePonto cartao : cartoes) {
            if (periodo.contem(cartao.getData())) total += cartao.getHorasExtras();
        }
        return total;
    }
}