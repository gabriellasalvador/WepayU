package br.ufal.ic.p2.wepayu.models;
 
import java.util.ArrayList;
import java.util.List;
 
/**
 * Empregado horista: recebe por hora trabalhada, registrada via cartoes de ponto,
 * com pagamento semanal (toda sexta-feira, ver Facade.isDiaPagamentoHorista).
 * Horas ate 8 por cartao contam como normais e o excedente como extra (pago a 1.5x).
 * Diferente da superclasse, aqui addCartao/getCartoes realmente funcionam em vez
 * de lancar excecao, pois so faz sentido para esse tipo.
 */
public class EmpregadoHorista extends Empregado {
    // Historico de todos os cartoes de ponto (data + horas) lancados para este empregado.
    private List<CartaoDePonto> cartoes = new ArrayList<>();
 
    public EmpregadoHorista(String id, String nome, String endereco, double salario) {
        super(id, nome, endereco, salario);
    }
 
    @Override
    public String getTipo() { return "horista"; }
 
    @Override
    public void addCartao(CartaoDePonto cartao) {
        cartoes.add(cartao);
    }
 
    @Override
    public List<CartaoDePonto> getCartoes() {
        return cartoes;
    }
}
 


