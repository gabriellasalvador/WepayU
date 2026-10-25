package br.ufal.ic.p2.wepayu.models.sindicato;

import br.ufal.ic.p2.wepayu.excecoes.sindicato.TaxaSindicalNegativaException;

import br.ufal.ic.p2.wepayu.models.Periodo;
import br.ufal.ic.p2.wepayu.models.TaxaServico;
import br.ufal.ic.p2.wepayu.util.Dinheiro;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Empregado filiado ao sindicato. Guarda os dados da filiacao (id no sindicato e
 * taxa sindical diaria, que nao mudam), as taxas de servico lancadas e a divida
 * acumulada que ainda nao foi descontada em folha (estes dois ultimos mudam ao
 * longo do tempo). E tambem esta classe que sabe calcular o desconto da folha.
 *
 * Para mudar o id ou a taxa sindical cria-se um novo Filiado (Empregado.setSindicato).
 */
public class Filiado implements FiliacaoSindical {
    private final String idSindicato;
    private final double taxaSindical;
    // Saldo devedor de taxa sindical/taxas de servico ainda nao descontado em folha;
    // e atualizado por calcularDesconto a cada folha rodada.
    private double dividaSindical;
    private List<TaxaServico> taxasServico;

    /**
     * Filia o empregado: sem divida e sem taxas de servico lancadas.
     *
     * @throws TaxaSindicalNegativaException se a taxa sindical for negativa
     */
    public Filiado(String idSindicato, double taxaSindical) throws TaxaSindicalNegativaException {
        if (taxaSindical < 0) throw new TaxaSindicalNegativaException();
        this.idSindicato = idSindicato;
        this.taxaSindical = taxaSindical;
        this.dividaSindical = 0;
        this.taxasServico = new ArrayList<>();
    }

    // Construtor usado por copiar(): duplica tambem a lista de taxas de servico.
    private Filiado(Filiado outro) {
        this.idSindicato = outro.idSindicato;
        this.taxaSindical = outro.taxaSindical;
        this.dividaSindical = outro.dividaSindical;
        this.taxasServico = new ArrayList<>(outro.taxasServico);
    }

    @Override
    public boolean isFiliado() {
        return true;
    }

    @Override
    public boolean temIdSindicato(String id) {
        return idSindicato.equals(id);
    }

    @Override
    public String getIdSindicato() {
        return idSindicato;
    }

    @Override
    public double getTaxaSindical() {
        return taxaSindical;
    }

    @Override
    public double totalTaxasEm(Periodo periodo) {
        double total = 0;
        for (TaxaServico taxa : taxasServico) {
            if (periodo.contem(taxa.getData())) total += taxa.getValor();
        }
        return total;
    }

    @Override
    public void registrarTaxaServico(TaxaServico taxa) {
        taxasServico.add(taxa);
    }

    @Override
    public double calcularDesconto(LocalDate inicioExclusive, LocalDate fimInclusive,
                                   int diasNoPeriodo, double bruto, boolean aplicarMutacao) {
        double cobrancaPeriodo = taxaSindical * diasNoPeriodo;
        for (TaxaServico t : taxasServico) {
            LocalDate d = t.getData();
            if (d.isAfter(inicioExclusive) && !d.isAfter(fimInclusive)) {
                cobrancaPeriodo += t.getValor();
            }
        }

        double dividaTotal = dividaSindical + cobrancaPeriodo;
        double descontosAplicados = Dinheiro.truncar(Math.min(dividaTotal, bruto));

        if (aplicarMutacao) {
            dividaSindical = Dinheiro.truncar(dividaTotal - descontosAplicados);
        }

        return descontosAplicados;
    }

    @Override
    public FiliacaoSindical copiar() {
        return new Filiado(this);
    }
}