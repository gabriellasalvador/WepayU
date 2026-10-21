package br.ufal.ic.p2.wepayu.models.pagamento;

import br.ufal.ic.p2.wepayu.Exception.pagamento.MetodoPagamentoInvalidoException;

/**
 * Fabrica (padrao Factory) que transforma o nome de uma forma de pagamento,
 * recebido como texto pela Facade, no objeto MetodoPagamento correspondente.
 * Concentra em um unico lugar a decisao "qual classe instanciar", em vez de
 * espalhar if/else por Strings pelo sistema.
 *
 * Atende apenas as formas que nao precisam de dados extras ("emMaos" e
 * "correios"). O pagamento em banco exige banco, agencia e conta e por isso e
 * criado direto com new Banco(...) pela Facade.
 */
public final class FabricaMetodoPagamento {

    private FabricaMetodoPagamento() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    /**
     * Cria a forma de pagamento sem dados extras identificada por "nome".
     *
     * @throws MetodoPagamentoInvalidoException se o nome for nulo ou diferente
     *         de "emMaos" e "correios" (inclusive "banco", que precisa de dados)
     */
    public static MetodoPagamento criarSemDadosExtras(String nome) throws MetodoPagamentoInvalidoException {
        if ("emMaos".equals(nome)) return new EmMaos();
        if ("correios".equals(nome)) return new Correios();
        throw new MetodoPagamentoInvalidoException();
    }
}