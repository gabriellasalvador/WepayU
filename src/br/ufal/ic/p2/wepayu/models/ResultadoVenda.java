package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.excecoes.lancamentos.ValorNaoPositivoException;

import java.time.LocalDate;
import java.io.Serializable;

/**
 * Registro de uma venda realizada por um empregado comissionado: valor vendido
 * em uma data especifica. Cada lancaVenda(...) na Facade cria um destes e
 * adiciona a lista de vendas do empregado (EmpregadoComissionado.vendas), usada
 * depois para calcular a comissao do periodo na folha de pagamento.
 *
 * Implementa Serializable porque faz parte do estado do sistema salvo pela
 * Facade nas "fotos" usadas no undo/redo/persistencia.
 */
public class ResultadoVenda implements Serializable{
    private LocalDate data;
    private double valor;

    /**
     * @throws ValorNaoPositivoException se o valor for zero ou negativo
     */
    public ResultadoVenda(LocalDate data, double valor) throws ValorNaoPositivoException {
        if (valor <= 0) throw new ValorNaoPositivoException();
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() { return data; }
    public double getValor() { return valor; }
}