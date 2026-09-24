package br.ufal.ic.p2.wepayu.models;
 
/**
 * Empregado assalariado: recebe um salario fixo mensal, pago no ultimo dia util
 * de cada mes (ver Facade.isDiaPagamentoAssalariado), sem cartao de ponto nem
 * comissao. Toda a logica comum (nome, endereco, sindicalizacao, forma de
 * pagamento) fica na superclasse Empregado; aqui so se define o tipo.
 */
public class EmpregadoAssalariado extends Empregado {
 
    public EmpregadoAssalariado(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario);
    }
 
    @Override
    public String getTipo() {
        return "assalariado";
    }
}
 


