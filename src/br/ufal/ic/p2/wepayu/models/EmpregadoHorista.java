package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;
import java.util.List;

public class EmpregadoHorista extends Empregado {
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