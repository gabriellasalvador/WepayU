package br.ufal.ic.p2.wepayu.models.sindicato;

import br.ufal.ic.p2.wepayu.excecoes.tipoempregado.EmpregadoNaoEhSindicalizadoException;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

import java.time.LocalDate;
import java.util.List;

/**
 * Empregado que nao pertence ao sindicato. E a situacao inicial de todo
 * empregado novo. Nao guarda dado nenhum (por isso pode ser compartilhado sem
 * risco): o desconto da folha e zero e qualquer pedido de dados de sindicato
 * resulta em EmpregadoNaoEhSindicalizadoException.
 */
public class NaoFiliado implements FiliacaoSindical {

    @Override
    public boolean isFiliado() {
        return false;
    }

    @Override
    public boolean temIdSindicato(String id) {
        return false;
    }

    @Override
    public String getIdSindicato() throws EmpregadoNaoEhSindicalizadoException {
        throw new EmpregadoNaoEhSindicalizadoException();
    }

    @Override
    public double getTaxaSindical() throws EmpregadoNaoEhSindicalizadoException {
        throw new EmpregadoNaoEhSindicalizadoException();
    }

    @Override
    public List<TaxaServico> getTaxasServico() throws EmpregadoNaoEhSindicalizadoException {
        throw new EmpregadoNaoEhSindicalizadoException();
    }

    @Override
    public void registrarTaxaServico(TaxaServico taxa) throws EmpregadoNaoEhSindicalizadoException {
        throw new EmpregadoNaoEhSindicalizadoException();
    }

    @Override
    public double calcularDesconto(LocalDate inicioExclusive, LocalDate fimInclusive,
                                   int diasNoPeriodo, double bruto, boolean aplicarMutacao) {
        return 0;
    }

    @Override
    public FiliacaoSindical copiar() {
        // Nao tem estado, entao uma nova instancia equivale a qualquer outra.
        return new NaoFiliado();
    }
}