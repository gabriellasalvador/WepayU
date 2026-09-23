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
import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Deque;
import java.util.ArrayDeque;

public class Facade {

    private Map<String, Empregado> empregados = new HashMap<>();
    private int proximoId = 1;
    private Deque<byte[]> pilhaUndo = new ArrayDeque<>();
    private Deque<byte[]> pilhaRedo = new ArrayDeque<>();
    private boolean sistemaEncerrado = false;


    public void zerarSistema() {
        byte[] fotoAntes = tirarFoto();
        empregados.clear();
        folhasGeradas.clear();
        proximoId = 1;
        registrarComando(fotoAntes);
    }

    public void encerrarSistema() {
        sistemaEncerrado = true;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
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

        String id = String.valueOf(proximoId++);
        Empregado empregado = tipo.equals("horista")
                ? new EmpregadoHorista(id, nome, endereco, salarioConvertido)
                : new EmpregadoAssalariado(id, nome, endereco, salarioConvertido);
        empregados.put(id, empregado);
        registrarComando(fotoAntes);
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
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

        String id = String.valueOf(proximoId++);
        Empregado empregado = new EmpregadoComissionado(id, nome, endereco, salarioConvertido, comissaoConvertida);
        empregados.put(id, empregado);
        registrarComando(fotoAntes);
        return id;
    }

    public void removerEmpregado(String emp) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        if (!empregados.containsKey(emp)) throw new EmpregadoNaoExisteException();
        registrarComando(fotoAntes);
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

    public int getNumeroDeEmpregados() {
        return empregados.size();
    }
    private LocalDate parseData(String data) throws Exception {
        String[] partes = data.split("/");
        int dia = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        int ano = Integer.parseInt(partes[2]);
        return LocalDate.of(ano, mes, dia);
    }

    public void lancaCartao(String emp, String data, String horas) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
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
        registrarComando(fotoAntes);
    }

    public String getEmpregadoPorNome(String nome, String indice) throws ValidacaoException {
        int indiceConvertido = Integer.parseInt(indice);

        List<Empregado> encontrados = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e.getNome().contains(nome)) {
                encontrados.add(e);
            }
        }
        encontrados.sort(Comparator.comparing(e -> Integer.parseInt(e.getId())));

        if (indiceConvertido < 1 || indiceConvertido > encontrados.size()) {
            throw new NaoHaEmpregadoComEsseNomeException();
        }

        return encontrados.get(indiceConvertido - 1).getId();
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
        byte[] fotoAntes = tirarFoto();
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
        registrarComando(fotoAntes);
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

    private byte[] tirarFoto() {
        try {
            ByteArrayOutputStream bufferDeBytes = new ByteArrayOutputStream();
            ObjectOutputStream escritorDeObjetos = new ObjectOutputStream(bufferDeBytes);
            escritorDeObjetos.writeObject(empregados);
            escritorDeObjetos.writeObject(folhasGeradas);
            escritorDeObjetos.writeInt(proximoId);
            escritorDeObjetos.close();
            return bufferDeBytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao tirar foto do sistema: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void restaurarFoto(byte[] foto) {
        try {
            ByteArrayInputStream bufferDeBytes = new ByteArrayInputStream(foto);
            ObjectInputStream leitorDeObjetos = new ObjectInputStream(bufferDeBytes);
            empregados = (Map<String, Empregado>) leitorDeObjetos.readObject();
            folhasGeradas = (Map<LocalDate, String>) leitorDeObjetos.readObject();
            proximoId = leitorDeObjetos.readInt();
            leitorDeObjetos.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao restaurar foto do sistema: " + e.getMessage());
        }
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
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
        registrarComando(fotoAntes);
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
        byte[] fotoAntes = tirarFoto();
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

        registrarComando(fotoAntes);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String valorExtra) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
        Empregado empregado = buscarEmpregado(emp);
        if (!atributo.equals("tipo")) throw new AtributoNaoExisteException();

        if (valor.equals("horista")) {
            trocarTipo(empregado, valor, valorExtra, null);
        } else if (valor.equals("comissionado")) {
            trocarTipo(empregado, valor, null, valorExtra);
        } else {
            throw new TipoInvalidoException();
        }
        registrarComando(fotoAntes);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
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
        registrarComando(fotoAntes);
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
        Empregado empregado = buscarEmpregado(emp);

        if (banco == null || banco.isEmpty()) throw new BancoNuloException();
        if (agencia == null || agencia.isEmpty()) throw new AgenciaNulaException();
        if (contaCorrente == null || contaCorrente.isEmpty()) throw new ContaCorrenteNulaException();

        empregado.definirPagamentoBanco(banco, agencia, contaCorrente);
        registrarComando(fotoAntes);
    }

// ===================== FOLHA DE PAGAMENTO (US7) =====================

    private static final LocalDate PRIMEIRO_PAGAMENTO_COMISSIONADO = LocalDate.of(2005, 1, 14);
    private Map<LocalDate, String> folhasGeradas = new HashMap<>();

    private static class ResultadoFolha {
        String relatorio;
        double total;
    }

    private String repete(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private String bordaTopo() {
        return repete('=', 127);
    }

    private String bordaCategoria(String titulo) {
        String meio = repete('=', 21) + " " + titulo + " ";
        return meio + repete('=', 127 - meio.length());
    }

    private double truncar(double valor) {
        return Math.floor(valor * 100 + 1e-9) / 100.0;
    }

    private boolean isDiaPagamentoHorista(LocalDate data) {
        return data.getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    private LocalDate ultimoDiaUtilDoMes(YearMonth mes) {
        LocalDate dia = mes.atEndOfMonth();
        while (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.minusDays(1);
        }
        return dia;
    }

    private boolean isDiaPagamentoAssalariado(LocalDate data) {
        return data.equals(ultimoDiaUtilDoMes(YearMonth.from(data)));
    }

    private boolean isDiaPagamentoComissionado(LocalDate data) {
        if (data.isBefore(PRIMEIRO_PAGAMENTO_COMISSIONADO)) return false;
        return ChronoUnit.DAYS.between(PRIMEIRO_PAGAMENTO_COMISSIONADO, data) % 14 == 0;
    }

    private String metodoDescricao(Empregado e) {
        switch (e.getMetodoPagamento()) {
            case "banco":
                return e.getBanco() + ", Ag. " + e.getAgencia() + " CC " + e.getContaCorrente();
            case "correios":
                return "Correios, " + e.getEndereco();
            default:
                return "Em maos";
        }
    }

    // "aplicarMutacao" = true grava o novo saldo de divida no empregado (usado pelo rodaFolha);
// false so simula, sem persistir nada (usado pelo totalFolha, que e' so uma previa)
    private double calcularDescontoSindicato(Empregado e, LocalDate inicioExclusive, LocalDate fimInclusive,
                                             int diasNoPeriodo, double bruto, boolean aplicarMutacao) {
        if (!e.isSindicalizado()) return 0;

        double cobrancaPeriodo = e.getTaxaSindical() * diasNoPeriodo;
        for (TaxaServico t : e.getTaxasServico()) {
            LocalDate d = t.getData();
            if (d.isAfter(inicioExclusive) && !d.isAfter(fimInclusive)) {
                cobrancaPeriodo += t.getValor();
            }
        }

        double dividaTotal = e.getDividaSindical() + cobrancaPeriodo;
        double descontosAplicados = truncar(Math.min(dividaTotal, bruto));

        if (aplicarMutacao) {
            e.setDividaSindical(truncar(dividaTotal - descontosAplicados));
        }

        return descontosAplicados;
    }

    private ResultadoFolha calcularFolha(LocalDate data, boolean aplicarMutacao) {
        ResultadoFolha resultado = new ResultadoFolha();
        StringBuilder sb = new StringBuilder();

        String titulo = "FOLHA DE PAGAMENTO DO DIA " + data.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        sb.append(titulo).append("\r\n");
        sb.append(repete('=', titulo.length())).append("\r\n");
        sb.append("\r\n");

        double totalGeral = 0;

        // ---------- HORISTAS ----------
        List<Empregado> horistas = new ArrayList<>();
        for (Empregado e : empregados.values()) if ("horista".equals(e.getTipo())) horistas.add(e);
        horistas.sort(Comparator.comparing(Empregado::getNome));

        sb.append(bordaTopo()).append("\r\n");
        sb.append(bordaCategoria("HORISTAS")).append("\r\n");
        sb.append(bordaTopo()).append("\r\n");
        sb.append(String.format("%-36s %-5s %-5s %-13s %-9s %-15s %s",
                "Nome", "Horas", "Extra", "Salario Bruto", "Descontos", "Salario Liquido", "Metodo")).append("\r\n");
        sb.append(String.format("%-36s %5s %5s %13s %9s %15s %s",
                repete('=', 36), repete('=', 5), repete('=', 5), repete('=', 13), repete('=', 9), repete('=', 15), repete('=', 38))).append("\r\n");

        double totHoras = 0, totExtra = 0, totBrutoH = 0, totDescH = 0, totLiqH = 0;
        boolean diaHorista = isDiaPagamentoHorista(data);
        if (diaHorista) {
            LocalDate inicio = data.minusDays(7);
            for (Empregado e : horistas) {
                List<CartaoDePonto> cartoes;
                try { cartoes = e.getCartoes(); } catch (EmpregadoNaoEhHoristaException ex) { cartoes = new ArrayList<>(); }

                double normais = 0, extras = 0;
                for (CartaoDePonto c : cartoes) {
                    LocalDate d = c.getData();
                    if (d.isAfter(inicio) && !d.isAfter(data)) {
                        normais += Math.min(c.getHoras(), 8);
                        extras += Math.max(0, c.getHoras() - 8);
                    }
                }
                double bruto = truncar(normais * e.getSalario() + extras * e.getSalario() * 1.5);
                double descontos = calcularDescontoSindicato(e, inicio, data, 7, bruto, aplicarMutacao);
                double liquido = bruto - descontos;

                sb.append(String.format("%-36s %5s %5s %13s %9s %15s %s",
                        e.getNome(), formataHoras(normais), formataHoras(extras),
                        formataValor(bruto), formataValor(descontos), formataValor(liquido),
                        metodoDescricao(e))).append("\r\n");

                totHoras += normais; totExtra += extras; totBrutoH += bruto; totDescH += descontos; totLiqH += liquido;
            }
        }
        sb.append("\r\n");
        sb.append(String.format("%-36s %5s %5s %13s %9s %15s",
                "TOTAL HORISTAS", formataHoras(totHoras), formataHoras(totExtra),
                formataValor(totBrutoH), formataValor(totDescH), formataValor(totLiqH))).append("\r\n");
        sb.append("\r\n");
        totalGeral += totBrutoH;

        // ---------- ASSALARIADOS ----------
        List<Empregado> assalariados = new ArrayList<>();
        for (Empregado e : empregados.values()) if ("assalariado".equals(e.getTipo())) assalariados.add(e);
        assalariados.sort(Comparator.comparing(Empregado::getNome));

        sb.append(bordaTopo()).append("\r\n");
        sb.append(bordaCategoria("ASSALARIADOS")).append("\r\n");
        sb.append(bordaTopo()).append("\r\n");
        sb.append(String.format("%-48s %-13s %-9s %-15s %s",
                "Nome", "Salario Bruto", "Descontos", "Salario Liquido", "Metodo")).append("\r\n");
        sb.append(String.format("%-48s %13s %9s %15s %s",
                repete('=', 48), repete('=', 13), repete('=', 9), repete('=', 15), repete('=', 38))).append("\r\n");

        double totBrutoA = 0, totDescA = 0, totLiqA = 0;
        boolean diaAssalariado = isDiaPagamentoAssalariado(data);
        if (diaAssalariado) {
            YearMonth mes = YearMonth.from(data);
            LocalDate inicioMes = mes.atDay(1).minusDays(1);
            int diasNoMes = mes.lengthOfMonth();
            for (Empregado e : assalariados) {
                double bruto = truncar(e.getSalario());
                double descontos = calcularDescontoSindicato(e, inicioMes, data, diasNoMes, bruto, aplicarMutacao);
                double liquido = bruto - descontos;

                sb.append(String.format("%-48s %13s %9s %15s %s",
                        e.getNome(), formataValor(bruto), formataValor(descontos), formataValor(liquido),
                        metodoDescricao(e))).append("\r\n");

                totBrutoA += bruto; totDescA += descontos; totLiqA += liquido;
            }
        }
        sb.append("\r\n");
        sb.append(String.format("%-48s %13s %9s %15s",
                "TOTAL ASSALARIADOS", formataValor(totBrutoA), formataValor(totDescA), formataValor(totLiqA))).append("\r\n");
        sb.append("\r\n");
        totalGeral += totBrutoA;

        // ---------- COMISSIONADOS ----------
        List<Empregado> comissionados = new ArrayList<>();
        for (Empregado e : empregados.values()) if ("comissionado".equals(e.getTipo())) comissionados.add(e);
        comissionados.sort(Comparator.comparing(Empregado::getNome));

        sb.append(bordaTopo()).append("\r\n");
        sb.append(bordaCategoria("COMISSIONADOS")).append("\r\n");
        sb.append(bordaTopo()).append("\r\n");
        sb.append(String.format("%-21s %-8s %-8s %-8s %-13s %-9s %-15s %s",
                "Nome", "Fixo", "Vendas", "Comissao", "Salario Bruto", "Descontos", "Salario Liquido", "Metodo")).append("\r\n");
        sb.append(String.format("%-21s %8s %8s %8s %13s %9s %15s %s",
                repete('=', 21), repete('=', 8), repete('=', 8), repete('=', 8), repete('=', 13), repete('=', 9), repete('=', 15), repete('=', 38))).append("\r\n");

        double totFixo = 0, totVendas = 0, totComissao = 0, totBrutoC = 0, totDescC = 0, totLiqC = 0;
        boolean diaComissionado = isDiaPagamentoComissionado(data);
        if (diaComissionado) {
            LocalDate inicio = data.minusDays(14);
            for (Empregado e : comissionados) {
                List<ResultadoVenda> vendas;
                try { vendas = e.getVendas(); } catch (EmpregadoNaoEhComissionadoException ex) { vendas = new ArrayList<>(); }

                double vendasPeriodo = 0;
                for (ResultadoVenda v : vendas) {
                    LocalDate d = v.getData();
                    if (d.isAfter(inicio) && !d.isAfter(data)) vendasPeriodo += v.getValor();
                }

                double fixo = truncar(e.getSalario() * 24 / 52);
                double comissaoValor;
                try { comissaoValor = truncar(vendasPeriodo * e.getComissao()); }
                catch (EmpregadoNaoEhComissionadoException ex) { comissaoValor = 0; }

                double bruto = truncar(fixo + comissaoValor);
                double descontos = calcularDescontoSindicato(e, inicio, data, 14, bruto, aplicarMutacao);
                double liquido = bruto - descontos;

                sb.append(String.format("%-21s %8s %8s %8s %13s %9s %15s %s",
                        e.getNome(), formataValor(fixo), formataValor(vendasPeriodo), formataValor(comissaoValor),
                        formataValor(bruto), formataValor(descontos), formataValor(liquido),
                        metodoDescricao(e))).append("\r\n");

                totFixo += fixo; totVendas += vendasPeriodo; totComissao += comissaoValor;
                totBrutoC += bruto; totDescC += descontos; totLiqC += liquido;
            }
        }
        sb.append("\r\n");
        sb.append(String.format("%-21s %8s %8s %8s %13s %9s %15s",
                "TOTAL COMISSIONADOS", formataValor(totFixo), formataValor(totVendas), formataValor(totComissao),
                formataValor(totBrutoC), formataValor(totDescC), formataValor(totLiqC))).append("\r\n");
        sb.append("\r\n");
        totalGeral += totBrutoC;

        sb.append("TOTAL FOLHA: ").append(formataValor(truncar(totalGeral))).append("\r\n");

        resultado.relatorio = sb.toString();
        resultado.total = truncar(totalGeral);
        return resultado;
    }

    public String totalFolha(String data) throws ValidacaoException {
        LocalDate dataConvertida;
        try { dataConvertida = parseData(data); } catch (Exception e) { throw new DataInvalidaException(); }
        return formataValor(calcularFolha(dataConvertida, false).total);
    }

    public void rodaFolha(String data, String saida) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
        LocalDate dataConvertida;
        try { dataConvertida = parseData(data); } catch (Exception e) { throw new DataInvalidaException(); }

        String relatorio = folhasGeradas.computeIfAbsent(dataConvertida, d -> calcularFolha(d, true).relatorio);

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(saida), "UTF-8"))) {
            out.print(relatorio);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever arquivo da folha: " + e.getMessage());
        }
        registrarComando(fotoAntes);
    }

    private void registrarComando(byte[] fotoAntes) {
        pilhaUndo.push(fotoAntes);
        pilhaRedo.clear();
    }

    public void undo() throws ValidacaoException {
        if (sistemaEncerrado) throw new SistemaEncerradoException();
        if (pilhaUndo.isEmpty()) throw new NaoHaComandoDesfazerException();

        byte[] estadoAtual = tirarFoto();
        byte[] estadoAnterior = pilhaUndo.pop();
        pilhaRedo.push(estadoAtual);
        restaurarFoto(estadoAnterior);
    }

    public void redo() throws ValidacaoException {
        if (sistemaEncerrado) throw new SistemaEncerradoException();
        if (pilhaRedo.isEmpty()) throw new NaoHaComandoRefazerException();

        byte[] estadoAtual = tirarFoto();
        byte[] estadoSeguinte = pilhaRedo.pop();
        pilhaUndo.push(estadoAtual);
        restaurarFoto(estadoSeguinte);
    }




}