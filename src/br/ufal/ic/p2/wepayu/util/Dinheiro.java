package br.ufal.ic.p2.wepayu.util;

/**
 * Operacoes auxiliares sobre valores monetarios, compartilhadas por varias
 * partes do sistema (calculo de salarios e descontos sindicais). Antes, esta
 * regra vivia como metodo privado da Facade; ao sair de la, ela pode ser usada
 * por qualquer classe sem depender da Facade.
 */
public final class Dinheiro {

    private Dinheiro() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    /**
     * Trunca (nao arredonda) um valor monetario para duas casas decimais,
     * evitando problemas de imprecisao de ponto flutuante com um pequeno epsilon.
     */
    public static double truncar(double valor) {
        return Math.floor(valor * 100 + 1e-9) / 100.0;
    }
}