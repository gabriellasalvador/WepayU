package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.lancamentos.ValorNaoPositivoException;

import java.time.LocalDate;
import java.io.Serializable;

/**
 * Registro de uma taxa de servico cobrada de um empregado sindicalizado em uma
 * data especifica. Cada lancaTaxaServico(...) na Facade cria um destes e adiciona
 * a lista de taxas do empregado filiado (Filiado), usada depois pelo calculo de
 * desconto sindical na folha de pagamento (Filiado.calcularDesconto).
 *
 * Implementa Serializable porque faz parte do estado do sistema salvo pela
 * Facade nas "fotos" usadas no undo/redo/persistencia.
 */
public class TaxaServico implements Serializable{
    private LocalDate data;
    private double valor;

    /**
     * @throws ValorNaoPositivoException se o valor for zero ou negativo
     */
    public TaxaServico(LocalDate data, double valor) throws ValorNaoPositivoException {
        if (valor <= 0) throw new ValorNaoPositivoException();
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() { return data; }
    public double getValor() { return valor; }
}