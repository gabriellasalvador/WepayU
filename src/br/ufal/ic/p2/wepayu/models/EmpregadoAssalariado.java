package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.models.agenda.AgendaMensalUltimoDiaUtil;
import br.ufal.ic.p2.wepayu.models.folha.CategoriaFolha;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.time.LocalDate;

/**
 * Empregado assalariado: recebe um salario fixo mensal, por padrao no ultimo dia util
 * de cada mes (agenda "mensal $", ver AgendaMensalUltimoDiaUtil), sem cartao de ponto
 * nem comissao. Toda a logica comum (nome, endereco, sindicalizacao, forma de
 * pagamento) fica na superclasse Empregado; aqui se define o tipo e como se calcula
 * o pagamento: a parte do salario mensal que cabe no periodo da agenda.
 */
public class EmpregadoAssalariado extends Empregado {

    public EmpregadoAssalariado(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario, new AgendaMensalUltimoDiaUtil());
    }

    @Override
    public String getTipo() {
        return "assalariado";
    }

    @Override
    public CategoriaFolha getCategoria() {
        return CategoriaFolha.ASSALARIADOS;
    }

    /** O unico valor e o bruto: a parte do salario mensal que corresponde ao periodo da agenda. */
    @Override
    protected double[] calcularParcelas(LocalDate inicioExclusive, LocalDate fimInclusive) {
        double bruto = Dinheiro.truncar(getAgenda().parteFixaDoSalario(getSalario()));
        return new double[] { bruto };
    }
}