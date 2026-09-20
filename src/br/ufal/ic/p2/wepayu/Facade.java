package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.CartaoDePonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.models.ResultadoVenda;

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
            case "comissao": return formataValor(empregado.getComissao());
            case "sindicalizado": return String.valueOf(empregado.isSindicalizado());
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
}