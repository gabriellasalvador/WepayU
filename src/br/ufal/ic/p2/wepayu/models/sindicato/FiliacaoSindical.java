package br.ufal.ic.p2.wepayu.models.sindicato;

import br.ufal.ic.p2.wepayu.excecoes.tipoempregado.EmpregadoNaoEhSindicalizadoException;
import br.ufal.ic.p2.wepayu.models.Periodo;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Situacao de um empregado perante o sindicato (padrao State com Null Object).
 * Existem exatamente duas situacoes, cada uma em sua classe:
 *
 *  - Filiado: guarda id no sindicato, taxa sindical, taxas de servico e divida,
 *    e sabe calcular o desconto da folha;
 *  - NaoFiliado: nao guarda nada, o desconto e sempre zero e qualquer consulta a
 *    dados de sindicato lanca EmpregadoNaoEhSindicalizadoException.
 *
 * Todo empregado possui sempre uma FiliacaoSindical (comeca como NaoFiliado), entao
 * nunca existe referencia nula, e nem a Empregado nem a Facade precisam de flag
 * nem de "if (filiado)": basta chamar os metodos e cada classe faz o que lhe cabe.
 * Filiar ou desfiliar um empregado significa trocar o objeto
 * (Empregado.setSindicato), do mesmo jeito que se troca a forma de pagamento.
 * Ao trocar para NaoFiliado, divida e taxas de servico do objeto antigo deixam de
 * existir junto com ele.
 *
 * Implementa Serializable porque faz parte do estado do empregado salvo nas
 * "fotos" de undo/redo e na persistencia (estado.dat).
 */
public interface FiliacaoSindical extends Serializable {

    /** Informa se o empregado esta filiado ao sindicato. */
    boolean isFiliado();

    /**
     * Id do empregado dentro do sindicato.
     *
     * @throws EmpregadoNaoEhSindicalizadoException se nao estiver filiado
     */
    String getIdSindicato() throws EmpregadoNaoEhSindicalizadoException;

    /**
     * Informa se este e o id do empregado no sindicato. Quem nao esta filiado nao
     * tem id, entao responde sempre false (sem lancar excecao).
     */
    boolean temIdSindicato(String id);

    /**
     * Taxa sindical cobrada por dia.
     *
     * @throws EmpregadoNaoEhSindicalizadoException se nao estiver filiado
     */
    double getTaxaSindical() throws EmpregadoNaoEhSindicalizadoException;

    /**
     * Soma das taxas de servico lancadas dentro do periodo [inicio, fim). As taxas
     * ficam guardadas dentro do proprio objeto; novas taxas so entram por
     * registrarTaxaServico, para que toda mudanca de estado passe por um comando
     * da Facade (e, portanto, entre no undo/redo).
     *
     * @throws EmpregadoNaoEhSindicalizadoException se nao estiver filiado
     */
    double totalTaxasEm(Periodo periodo) throws EmpregadoNaoEhSindicalizadoException;

    /**
     * Registra uma taxa de servico cobrada do empregado em uma data.
     *
     * @throws EmpregadoNaoEhSindicalizadoException se nao estiver filiado
     */
    void registrarTaxaServico(TaxaServico taxa) throws EmpregadoNaoEhSindicalizadoException;

    /**
     * Calcula o desconto do sindicato a ser aplicado no salario bruto, somando a
     * taxa sindical do periodo com as taxas de servico lancadas dentro dele, e
     * descontando isso (limitado ao bruto) da divida acumulada. Para quem nao
     * esta filiado o desconto e zero.
     *
     * Quando aplicarMutacao e true, o novo saldo devedor e gravado (usado pelo
     * rodaFolha); quando false, o calculo e apenas uma simulacao que nao altera
     * estado (usado pelo totalFolha, que e so uma previa).
     *
     * @param inicioExclusive dia anterior ao primeiro dia do periodo (nao incluso)
     * @param fimInclusive    ultimo dia do periodo (incluso)
     * @param diasNoPeriodo   quantidade de dias do periodo, para a taxa sindical diaria
     * @param bruto           salario bruto do periodo, limite do desconto
     */
    double calcularDesconto(LocalDate inicioExclusive, LocalDate fimInclusive,
                            int diasNoPeriodo, double bruto, boolean aplicarMutacao);

    /**
     * Devolve uma copia independente desta situacao, usada quando o tipo do
     * empregado e trocado: o novo empregado nao pode compartilhar estado mutavel
     * (divida, lista de taxas) com o antigo.
     */
    FiliacaoSindical copiar();
}