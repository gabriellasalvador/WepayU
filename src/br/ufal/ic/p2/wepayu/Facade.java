package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.CartaoDePonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.models.ResultadoVenda;
import br.ufal.ic.p2.wepayu.models.TaxaServico;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facade {

    private Map<String, Empregado> empregados = new HashMap<>();

    public void zerarSistema() {
        empregados.clear();
    }

    public void encerrarSistema() {
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws ValidacaoException {
        if (nome == null || nome.isEmpty()) throw new NomeNuloException();
        if (endereco == null || endereco.isEmpty()) throw new EnderecoNuloException();
        if (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new TipoInvalidoException();
        }
        if (tipo.equals("comissionado")) throw new TipoNaoAplicavelException();
        if (salario == null || salario.isEmpty()) throw new SalarioNuloException();

        double salarioConvertido;
        try {
            salarioConvertido = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException();
        }
        if (salarioConvertido < 0) throw new SalarioNegativoException();

        String id = String.valueOf(empregados.size() + 1);
        Empregado empregado = tipo.equals("horista")
                ? new EmpregadoHorista(id, nome, endereco, salarioConvertido)
                : new EmpregadoAssalariado(id, nome, endereco, salarioConvertido);
        empregados.put(id, empregado);
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws ValidacaoException {
        if (nome == null || nome.isEmpty()) throw new NomeNuloException();
        if (endereco == null || endereco.isEmpty()) throw new EnderecoNuloException();
        if (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new TipoInvalidoException();
        }
        if (!tipo.equals("comissionado")) throw new TipoNaoAplicavelException();
        if (salario == null || salario.isEmpty()) throw new SalarioNuloException();

        double salarioConvertido;
        try {
            salarioConvertido = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException();
        }
        if (salarioConvertido < 0) throw new SalarioNegativoException();
        if (comissao == null || comissao.isEmpty()) throw new ComissaoNulaException();

        double comissaoConvertida;
        try {
            comissaoConvertida = Double.parseDouble(comissao.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ComissaoNaoNumericaException();
        }
        if (comissaoConvertida < 0) throw new ComissaoNegativaException();

        String id = String.valueOf(empregados.size() + 1);
        Empregado empregado = new EmpregadoComissionado(id, nome, endereco, salarioConvertido, comissaoConvertida);
        empregados.put(id, empregado);
        return id;
    }

    public void removerEmpregado(String emp) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        if (!empregados.containsKey(emp)) throw new EmpregadoNaoExisteException();
        empregados.remove(emp);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();

        switch (atributo) {
            case "nome": return empregado.getNome();
            case "endereco": return empregado.getEndereco();
            case "tipo": return empregado.getTipo();
            case "salario": return formataValor(empregado.getSalario());
            case "comissao":
                if (!"comissionado".equals(empregado.getTipo())) throw new EmpregadoNaoEhComissionadoException();
                return formataValor(empregado.getComissao());
            case "sindicalizado": return String.valueOf(empregado.isSindicalizado());
            case "metodoPagamento":
                return empregado.getMetodoPagamento();
            case "banco":
                if (!"banco".equals(empregado.getMetodoPagamento())) throw new EmpregadoNaoRecebeEmBancoException();
                return empregado.getBanco();
            case "agencia":
                if (!"banco".equals(empregado.getMetodoPagamento())) throw new EmpregadoNaoRecebeEmBancoException();
                return empregado.getAgencia();
            case "contaCorrente":
                if (!"banco".equals(empregado.getMetodoPagamento())) throw new EmpregadoNaoRecebeEmBancoException();
                return empregado.getContaCorrente();
            case "idSindicato":
                if (!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();
                return empregado.getIdSindicato();
            case "taxaSindical":
                if (!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();
                return formataValor(empregado.getTaxaSindical());
            default: throw new AtributoNaoExisteException();
        }
    }

    private LocalDate parseData(String data) throws Exception {
        String[] partes = data.split("/");
        int dia = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        int ano = Integer.parseInt(partes[2]);
        return LocalDate.of(ano, mes, dia);
    }

    public void lancaCartao(String emp, String data, String horas) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();

        LocalDate dataConvertida;
        try {
            dataConvertida = parseData(data);
        } catch (Exception e) {
            throw new DataInvalidaException();
        }
        double horasConvertidas = Double.parseDouble(horas.replace(",", "."));
        if (horasConvertidas <= 0) throw new HorasNaoPositivasException();

        empregado.addCartao(new CartaoDePonto(dataConvertida, horasConvertidas));
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        List<CartaoDePonto> cartoes = empregado.getCartoes();

        LocalDate inicio;
        try { inicio = parseData(dataInicial); } catch (Exception e) { throw new DataInicialInvalidaException(); }
        LocalDate fim;
        try { fim = parseData(dataFinal); } catch (Exception e) { throw new DataFinalInvalidaException(); }
        if (inicio.isAfter(fim)) throw new DataInicialPosteriorException();

        double total = 0;
        for (CartaoDePonto cartao : cartoes) {
            LocalDate data = cartao.getData();
            if (!data.isBefore(inicio) && data.isBefore(fim)) {
                total += Math.min(cartao.getHoras(), 8);
            }
        }
        return formataHoras(total);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        List<CartaoDePonto> cartoes = empregado.getCartoes();

        LocalDate inicio;
        try { inicio = parseData(dataInicial); } catch (Exception e) { throw new DataInicialInvalidaException(); }
        LocalDate fim;
        try { fim = parseData(dataFinal); } catch (Exception e) { throw new DataFinalInvalidaException(); }
        if (inicio.isAfter(fim)) throw new DataInicialPosteriorException();

        double total = 0;
        for (CartaoDePonto cartao : cartoes) {
            LocalDate data = cartao.getData();
            if (!data.isBefore(inicio) && data.isBefore(fim)) {
                total += Math.max(0, cartao.getHoras() - 8);
            }
        }
        return formataHoras(total);
    }

    public void lancaVenda(String emp, String data, String valor) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();

        LocalDate dataConvertida;
        try {
            dataConvertida = parseData(data);
        } catch (Exception e) {
            throw new DataInvalidaException();
        }
        double valorConvertido = Double.parseDouble(valor.replace(",", "."));
        if (valorConvertido <= 0) throw new ValorNaoPositivoException();

        empregado.addVenda(new ResultadoVenda(dataConvertida, valorConvertido));
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        List<ResultadoVenda> vendas = empregado.getVendas();

        LocalDate inicio;
        try { inicio = parseData(dataInicial); } catch (Exception e) { throw new DataInicialInvalidaException(); }
        LocalDate fim;
        try { fim = parseData(dataFinal); } catch (Exception e) { throw new DataFinalInvalidaException(); }
        if (inicio.isAfter(fim)) throw new DataInicialPosteriorException();

        double total = 0;
        for (ResultadoVenda venda : vendas) {
            LocalDate data = venda.getData();
            if (!data.isBefore(inicio) && data.isBefore(fim)) {
                total += venda.getValor();
            }
        }
        return formataValor(total);
    }

    private String formataValor(double valor) {
        return String.format(java.util.Locale.US, "%.2f", valor).replace(".", ",");
    }

    private String formataHoras(double valor) {
        if (valor == Math.floor(valor)) {
            return String.valueOf((int) valor);
        }
        String formatado = String.format(java.util.Locale.US, "%.2f", valor).replace(".", ",");
        if (formatado.endsWith("0")) {
            formatado = formatado.substring(0, formatado.length() - 1);
        }
        return formatado;
    }

    private Empregado buscarPorIdSindicato(String membro) throws ValidacaoException {
        for (Empregado empregado : empregados.values()) {
            if (empregado.isSindicalizado() && membro.equals(empregado.getIdSindicato())) {
                return empregado;
            }
        }
        throw new MembroNaoExisteException();
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws ValidacaoException {
        if (membro == null || membro.isEmpty()) throw new IdentificacaoMembroNulaException();
        Empregado empregado = buscarPorIdSindicato(membro);

        LocalDate dataConvertida;
        try {
            dataConvertida = parseData(data);
        } catch (Exception e) {
            throw new DataInvalidaException();
        }
        double valorConvertido = Double.parseDouble(valor.replace(",", "."));
        if (valorConvertido <= 0) throw new ValorNaoPositivoException();

        empregado.addTaxaServico(new TaxaServico(dataConvertida, valorConvertido));
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        if (!empregado.isSindicalizado()) throw new EmpregadoNaoEhSindicalizadoException();

        LocalDate inicio;
        try { inicio = parseData(dataInicial); } catch (Exception e) { throw new DataInicialInvalidaException(); }
        LocalDate fim;
        try { fim = parseData(dataFinal); } catch (Exception e) { throw new DataFinalInvalidaException(); }
        if (inicio.isAfter(fim)) throw new DataInicialPosteriorException();

        double total = 0;
        for (TaxaServico taxa : empregado.getTaxasServico()) {
            LocalDate data = taxa.getData();
            if (!data.isBefore(inicio) && data.isBefore(fim)) {
                total += taxa.getValor();
            }
        }
        return formataValor(total);
    }

    private Empregado buscarEmpregado(String emp) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        return empregado;
    }

    private void trocarTipo(Empregado antigo, String novoTipo, String novoSalario, String novaComissao) throws ValidacaoException {
        double salarioFinal = antigo.getSalario();
        if (novoSalario != null) {
            try {
                salarioFinal = Double.parseDouble(novoSalario.replace(",", "."));
            } catch (NumberFormatException e) {
                throw new SalarioNaoNumericoException();
            }
        }

        Empregado novo;
        if (novoTipo.equals("horista")) {
            novo = new EmpregadoHorista(antigo.getId(), antigo.getNome(), antigo.getEndereco(), salarioFinal);
        } else if (novoTipo.equals("assalariado")) {
            novo = new EmpregadoAssalariado(antigo.getId(), antigo.getNome(), antigo.getEndereco(), salarioFinal);
        } else if (novoTipo.equals("comissionado")) {
            double comissaoFinal = 0;
            if (novaComissao != null) {
                comissaoFinal = Double.parseDouble(novaComissao.replace(",", "."));
            }
            novo = new EmpregadoComissionado(antigo.getId(), antigo.getNome(), antigo.getEndereco(), salarioFinal, comissaoFinal);
        } else {
            throw new TipoInvalidoException();
        }

        novo.copiarEstadoComumDe(antigo);
        empregados.put(antigo.getId(), novo);
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws ValidacaoException {
        Empregado empregado = buscarEmpregado(emp);

        switch (atributo) {
            case "nome":
                if (valor == null || valor.isEmpty()) throw new NomeNuloException();
                empregado.setNome(valor);
                break;
            case "endereco":
                if (valor == null || valor.isEmpty()) throw new EnderecoNuloException();
                empregado.setEndereco(valor);
                break;
            case "salario":
                if (valor == null || valor.isEmpty()) throw new SalarioNuloException();
                double salarioConvertido;
                try {
                    salarioConvertido = Double.parseDouble(valor.replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new SalarioNaoNumericoException();
                }
                if (salarioConvertido < 0) throw new SalarioNegativoException();
                empregado.setSalario(salarioConvertido);
                break;
            case "comissao":
                if (valor == null || valor.isEmpty()) throw new ComissaoNulaException();
                double comissaoConvertida;
                try {
                    comissaoConvertida = Double.parseDouble(valor.replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new ComissaoNaoNumericaException();
                }
                if (comissaoConvertida < 0) throw new ComissaoNegativaException();
                empregado.setComissao(comissaoConvertida);
                break;
            case "tipo":
                trocarTipo(empregado, valor, null, null);
                break;
            case "metodoPagamento":
                if (valor.equals("emMaos")) {
                    empregado.definirPagamentoEmMaos();
                } else if (valor.equals("correios")) {
                    empregado.definirPagamentoCorreios();
                } else {
                    throw new MetodoPagamentoInvalidoException();
                }
                break;
            case "sindicalizado":
                if (valor.equals("false")) {
                    empregado.dessindicalizar();
                } else {
                    throw new ValorSindicalizadoInvalidoException();
                }
                break;
            default:
                throw new AtributoNaoExisteException();
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String valorExtra) throws ValidacaoException {
        Empregado empregado = buscarEmpregado(emp);
        if (!atributo.equals("tipo")) throw new AtributoNaoExisteException();

        if (valor.equals("horista")) {
            trocarTipo(empregado, valor, valorExtra, null);
        } else if (valor.equals("comissionado")) {
            trocarTipo(empregado, valor, null, valorExtra);
        } else {
            throw new TipoInvalidoException();
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws ValidacaoException {
        Empregado empregado = buscarEmpregado(emp);

        if (idSindicato == null || idSindicato.isEmpty()) throw new IdentificacaoSindicatoNulaException();

        for (Empregado outro : empregados.values()) {
            if (outro != empregado && outro.isSindicalizado() && idSindicato.equals(outro.getIdSindicato())) {
                throw new IdentificacaoSindicatoDuplicadaException();
            }
        }

        if (taxaSindical == null || taxaSindical.isEmpty()) throw new TaxaSindicalNulaException();
        double taxaConvertida;
        try {
            taxaConvertida = Double.parseDouble(taxaSindical.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new TaxaSindicalNaoNumericaException();
        }
        if (taxaConvertida < 0) throw new TaxaSindicalNegativaException();

        empregado.sindicalizar(idSindicato, taxaConvertida);
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws ValidacaoException {
        Empregado empregado = buscarEmpregado(emp);

        if (banco == null || banco.isEmpty()) throw new BancoNuloException();
        if (agencia == null || agencia.isEmpty()) throw new AgenciaNulaException();
        if (contaCorrente == null || contaCorrente.isEmpty()) throw new ContaCorrenteNulaException();

        empregado.definirPagamentoBanco(banco, agencia, contaCorrente);
    }



}