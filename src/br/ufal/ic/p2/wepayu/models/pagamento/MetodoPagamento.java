package br.ufal.ic.p2.wepayu.models.pagamento;

import br.ufal.ic.p2.wepayu.excecoes.tipoempregado.EmpregadoNaoRecebeEmBancoException;

import java.io.Serializable;

/**
 * Estrategia (padrao Strategy) que representa a forma como um empregado recebe
 * o pagamento: em maos, pelos correios ou em banco. Cada forma e uma classe
 * propria que sabe se identificar e se descrever no relatorio da folha, entao
 * nem o Empregado nem a Facade precisam comparar Strings como "banco" ou
 * "correios" para decidir o que fazer.
 *
 * Para criar uma quarta forma de pagamento basta escrever uma nova classe que
 * implemente esta interface (principio aberto/fechado: nada do que ja existe
 * precisa ser alterado alem do ponto onde a nova forma e criada).
 *
 * Implementa Serializable porque o Empregado guarda uma instancia dela e e
 * serializado nas "fotos" de undo/redo e na persistencia em estado.dat.
 * As implementacoes sao imutaveis: para mudar a forma de pagamento, troca-se o
 * objeto inteiro (Empregado.setMetodoPagamento), nunca se altera um existente.
 */
public interface MetodoPagamento extends Serializable {

    /**
     * Nome da forma de pagamento como aparece no atributo "metodoPagamento"
     * do empregado: "emMaos", "correios" ou "banco".
     */
    String getNome();

    /**
     * Texto da coluna "Metodo" do relatorio da folha de pagamento.
     *
     * @param enderecoDoEmpregado endereco cadastrado do empregado (so os
     *                            Correios usam esse dado)
     */
    String descreverParaFolha(String enderecoDoEmpregado);

    /**
     * Nome do banco. Por padrao lanca excecao, pois so o pagamento em banco
     * possui esse dado; a classe Banco sobrescreve este metodo.
     */
    default String getBanco() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }

    /**
     * Numero da agencia. Por padrao lanca excecao; apenas Banco sobrescreve.
     */
    default String getAgencia() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }

    /**
     * Numero da conta corrente. Por padrao lanca excecao; apenas Banco sobrescreve.
     */
    default String getContaCorrente() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }
}