package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.Exception.cadastro.*;
import br.ufal.ic.p2.wepayu.Exception.lancamentos.*;
import br.ufal.ic.p2.wepayu.Exception.pagamento.AgenciaNulaException;
import br.ufal.ic.p2.wepayu.Exception.pagamento.BancoNuloException;
import br.ufal.ic.p2.wepayu.Exception.pagamento.ContaCorrenteNulaException;
import br.ufal.ic.p2.wepayu.Exception.pagamento.MetodoPagamentoInvalidoException;
import br.ufal.ic.p2.wepayu.Exception.sindicato.*;
import br.ufal.ic.p2.wepayu.Exception.sistema.NaoHaComandoDesfazerException;
import br.ufal.ic.p2.wepayu.Exception.sistema.NaoHaComandoRefazerException;
import br.ufal.ic.p2.wepayu.Exception.sistema.SistemaEncerradoException;
import br.ufal.ic.p2.wepayu.Exception.tipoempregado.*;
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
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Fachada do sistema WePayU.
 *
 * Concentra toda a API publica que o EasyAccept (script de testes de aceitacao)
 * invoca por reflection: cadastro/alteracao/remocao de empregados, lancamento de
 * cartoes de ponto/vendas/taxas de servico, calculo e geracao da folha de pagamento
 * (US7), e o mecanismo de undo/redo com persistencia em disco (US8).
 *
 * Por isso varios metodos e o construtor aparecem como "nunca usados" para a IDE:
 * eles nao sao chamados diretamente por outra classe do projeto, e sim pelo
 * executor de testes externo, que e o contrato real dessa classe.
 */
public class Facade {

    // Repositorio principal: mapeia id do empregado -> objeto Empregado.
    private Map<String, Empregado> empregados = new HashMap<>();
    // Contador usado para gerar os proximos ids de empregado (1, 2, 3, ...).
    private int proximoId = 1;
    // Pilhas de "fotos" (snapshots serializados) do estado do sistema, usadas pelo undo/redo (US8).
    // Cada comando que muda o estado empilha em pilhaUndo a foto de ANTES de executar.
    private Deque<byte[]> pilhaUndo = new ArrayDeque<>();
    private Deque<byte[]> pilhaRedo = new ArrayDeque<>();
    // Marca se encerrarSistema() ja foi chamado; depois disso nenhum outro comando pode rodar.
    private boolean sistemaEncerrado = false;
    // Nome do arquivo onde o estado do sistema e persistido ao encerrar.
    private static final String ARQUIVO_ESTADO = "estado.dat";


    /**
     * Ao criar a fachada, tenta restaurar o estado salvo por um encerrarSistema()
     * anterior (arquivo estado.dat), simulando persistencia entre execucoes.
     */
    public Facade() {
        if (Files.exists(Paths.get(ARQUIVO_ESTADO))) {
            try {
                byte[] dadosSalvos = Files.readAllBytes(Paths.get(ARQUIVO_ESTADO));
                restaurarFoto(dadosSalvos);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao carregar estado salvo: " + e.getMessage());
            }
        }
    }


    /**
     * Reseta completamente o sistema (empregados, folhas geradas e contador de id),
     * registrando o estado anterior na pilha de undo.
     */
    public void zerarSistema() {
        byte[] fotoAntes = tirarFoto();
        empregados.clear();
        folhasGeradas.clear();
        proximoId = 1;
        registrarComando(fotoAntes);
    }

    /**
     * Encerra o sistema: bloqueia qualquer comando futuro (inclusive undo/redo) e
     * grava o estado atual em disco para poder ser recarregado numa proxima execucao.
     */
    public void encerrarSistema() {
        sistemaEncerrado = true;
        try {
            Files.write(Paths.get(ARQUIVO_ESTADO), tirarFoto());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar estado: " + e.getMessage());
        }
    }

    /**
     * Cria um empregado horista ou assalariado (nao comissionado).
     * Valida nome, endereco, tipo e salario antes de persistir o novo empregado.
     */
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

    /**
     * Sobrecarga usada para criar empregados do tipo comissionado, que exigem
     * tambem o valor da comissao alem do salario fixo.
     */
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

    /**
     * Remove um empregado existente do sistema pelo id.
     */
    public void removerEmpregado(String emp) throws ValidacaoException {
        byte[] fotoAntes = tirarFoto();
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        if (!empregados.containsKey(emp)) throw new EmpregadoNaoExisteException();
        registrarComando(fotoAntes);
        empregados.remove(emp);
    }

    /**
     * Le um atributo qualquer de um empregado a partir do nome do atributo (string),
     * despachando para o getter correspondente e validando pre-condicoes especificas
     * (ex.: "comissao" so faz sentido para comissionados, "banco" so para pagamento em banco).
     */
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

    /**
     * Retorna quantos empregados estao atualmente cadastrados no sistema.
     */
    public int getNumeroDeEmpregados() {
        return empregados.size();
    }

    /**
     * Converte uma data no formato "dd/mm/aaaa" (String) para LocalDate.
     * Qualquer falha de parsing/validade de data e propagada como Exception generica,
     * para que os metodos publicos a capturem e traduzam para a excecao de negocio adequada
     * (ex.: DataInvalidaException, DataInicialInvalidaException, DataFinalInvalidaException).
     */
    private LocalDate parseData(String data) throws Exception {
        String[] partes = data.split("/");
        int dia = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        int ano = Integer.parseInt(partes[2]);
        return LocalDate.of(ano, mes, dia);
    }

    /**
     * Lanca um cartao de ponto (horas trabalhadas em uma data) para um empregado horista.
     */
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

    /**
     * Busca o id do n-esimo (indice, 1-based) empregado cujo nome contem a substring informada,
     * ordenando os resultados por id (numerico) antes de aplicar o indice.
     */
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

    /**
     * Soma as horas normais (ate 8h por cartao) trabalhadas por um empregado
     * dentro do periodo [dataInicial, dataFinal), usada tipicamente para horistas.
     */
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

    /**
     * Soma as horas extras (acima de 8h por cartao) trabalhadas por um empregado
     * dentro do periodo [dataInicial, dataFinal).
     */
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

    /**
     * Lanca uma venda realizada por um empregado comissionado em uma data especifica.
     */
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

    /**
     * Soma o valor das vendas realizadas por um empregado comissionado
     * dentro do periodo [dataInicial, dataFinal).
     */
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

    /**
     * Formata um double como valor monetario no padrao brasileiro (duas casas decimais,
     * separador decimal com virgula). Ex.: 1234.5 -> "1234,50".
     */
    private String formataValor(double valor) {
        return String.format(java.util.Locale.US, "%.2f", valor).replace(".", ",");
    }

    /**
     * Formata um total de horas: se for um numero inteiro, exibe sem casas decimais;
     * caso contrario, exibe com uma ou duas casas decimais (removendo zero final desnecessario),
     * usando virgula como separador decimal.
     */
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

    /**
     * Localiza o empregado sindicalizado cujo idSindicato corresponde ao "membro" informado.
     * Usado pelos comandos relacionados a taxa de servico, que identificam o empregado
     * pelo id de sindicato e nao pelo id do empregado.
     */
    private Empregado buscarPorIdSindicato(String membro) throws ValidacaoException {
        for (Empregado empregado : empregados.values()) {
            if (empregado.isSindicalizado() && membro.equals(empregado.getIdSindicato())) {
                return empregado;
            }
        }
        throw new MembroNaoExisteException();
    }

    /**
     * Serializa (via ObjectOutputStream) uma "foto" completa do estado mutavel do sistema:
     * o mapa de empregados, as folhas ja geradas e o proximo id disponivel.
     * E a base do mecanismo de undo/redo/persistencia: cada snapshot vira um byte[]
     * que pode ser empilhado e restaurado depois.
     */
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

    /**
     * Desserializa uma "foto" gerada por tirarFoto() e substitui o estado atual
     * (empregados, folhasGeradas, proximoId) pelo estado salvo naquele snapshot.
     * Usado por undo, redo e pela restauracao do estado ao iniciar a Facade.
     */
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

    /**
     * Lanca uma taxa de servico cobrada por um empregado sindicalizado, identificado
     * pelo id de sindicato ("membro"), em uma data especifica.
     */
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

    /**
     * Soma o valor das taxas de servico lancadas para um empregado sindicalizado
     * dentro do periodo [dataInicial, dataFinal).
     */
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

    /**
     * Helper comum para localizar um empregado por id, lancando as excecoes
     * de validacao padrao (id nulo/vazio, empregado inexistente).
     */
    private Empregado buscarEmpregado(String emp) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        Empregado empregado = empregados.get(emp);
        if (empregado == null) throw new EmpregadoNaoExisteException();
        return empregado;
    }

    /**
     * Troca o tipo de um empregado (horista/assalariado/comissionado), criando uma
     * nova instancia da subclasse correta (mantendo o mesmo id) e copiando para ela
     * o estado comum do empregado antigo (nome, endereco, sindicalizacao, forma de
     * pagamento, etc). Permite opcionalmente definir salario/comissao no momento da troca.
     */
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

    /**
     * Altera um atributo simples de um empregado (nome, endereco, salario, comissao,
     * tipo, metodoPagamento ou sindicalizado=false), validando o novo valor conforme
     * o atributo escolhido.
     */
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

    /**
     * Sobrecarga de alteraEmpregado usada especificamente para trocar o tipo do
     * empregado ja informando, no mesmo comando, o novo salario (para horista)
     * ou a nova comissao (para comissionado).
     */
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

    /**
     * Sobrecarga de alteraEmpregado usada para sindicalizar um empregado, recebendo
     * o idSindicato (que deve ser unico entre os empregados sindicalizados) e a taxa
     * sindical a ser cobrada.
     */
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

    /**
     * Sobrecarga de alteraEmpregado usada para configurar o pagamento em banco,
     * recebendo banco, agencia e conta corrente.
     */
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

    // Data do primeiro pagamento de comissionados, usada como referencia para
    // calcular se uma data qualquer cai em um ciclo de pagamento quinzenal.
    private static final LocalDate PRIMEIRO_PAGAMENTO_COMISSIONADO = LocalDate.of(2005, 1, 14);
    // Cache das folhas ja geradas por data, para que rodaFolha() nao recalcule
    // (nem remova saldo de divida sindical duas vezes) se chamado de novo com a mesma data.
    private Map<LocalDate, String> folhasGeradas = new HashMap<>();

    /**
     * Estrutura simples para carregar, ao mesmo tempo, o texto do relatorio da folha
     * e o valor total pago nela.
     */
    private static class ResultadoFolha {
        String relatorio;
        double total;
    }

    /**
     * Monta uma string repetindo o caractere c, n vezes. Usado para desenhar as
     * linhas de borda (=====) do relatorio de folha.
     */
    private String repete(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    /**
     * Linha de borda "cheia" (127 caracteres de '=') usada no topo/rodape de cada
     * secao do relatorio de folha.
     */
    private String bordaTopo() {
        return repete('=', 127);
    }

    /**
     * Linha de borda com o titulo da categoria centralizado dentro de '=' (ex.: HORISTAS),
     * completando ate 127 caracteres.
     */
    private String bordaCategoria(String titulo) {
        String meio = repete('=', 21) + " " + titulo + " ";
        return meio + repete('=', 127 - meio.length());
    }

    /**
     * Trunca (nao arredonda) um valor monetario para duas casas decimais,
     * evitando problemas de imprecisao de ponto flutuante com um pequeno epsilon.
     */
    private double truncar(double valor) {
        return Math.floor(valor * 100 + 1e-9) / 100.0;
    }

    /**
     * Horistas sao pagos toda sexta-feira.
     */
    private boolean isDiaPagamentoHorista(LocalDate data) {
        return data.getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    /**
     * Calcula o ultimo dia util (nao sabado/domingo) do mes informado.
     */
    private LocalDate ultimoDiaUtilDoMes(YearMonth mes) {
        LocalDate dia = mes.atEndOfMonth();
        while (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.minusDays(1);
        }
        return dia;
    }

    /**
     * Assalariados sao pagos no ultimo dia util de cada mes.
     */
    private boolean isDiaPagamentoAssalariado(LocalDate data) {
        return data.equals(ultimoDiaUtilDoMes(YearMonth.from(data)));
    }

    /**
     * Comissionados sao pagos a cada 14 dias, a partir da data de referencia
     * PRIMEIRO_PAGAMENTO_COMISSIONADO (14/01/2005).
     */
    private boolean isDiaPagamentoComissionado(LocalDate data) {
        if (data.isBefore(PRIMEIRO_PAGAMENTO_COMISSIONADO)) return false;
        return ChronoUnit.DAYS.between(PRIMEIRO_PAGAMENTO_COMISSIONADO, data) % 14 == 0;
    }

    /**
     * Monta a descricao textual do metodo de pagamento de um empregado,
     * conforme exibido na coluna "Metodo" do relatorio de folha.
     */
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
    /**
     * Calcula o desconto do sindicato a ser aplicado no salario bruto de um empregado
     * sindicalizado, somando a taxa sindical fixa do periodo com as taxas de servico
     * lancadas dentro dele, e descontando isso (limitado ao bruto) da divida acumulada.
     * Quando aplicarMutacao=true, o novo saldo devedor e persistido no empregado;
     * quando false, o calculo e apenas uma simulacao (nao altera estado).
     */
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

    /**
     * Calcula (e formata como texto) a folha de pagamento completa referente a uma data,
     * percorrendo separadamente horistas, assalariados e comissionados, e so incluindo
     * valores de fato pagos para quem cai no dia de pagamento correspondente aquele tipo.
     * aplicarMutacao controla se o desconto sindical calculado deve ser persistido
     * (rodaFolha) ou apenas simulado (totalFolha).
     */
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

    /**
     * Retorna apenas o valor total da folha para uma data, sem gerar o relatorio
     * em arquivo e sem persistir descontos sindicais (calculo "de previa").
     */
    public String totalFolha(String data) throws ValidacaoException {
        LocalDate dataConvertida;
        try { dataConvertida = parseData(data); } catch (Exception e) { throw new DataInvalidaException(); }
        return formataValor(calcularFolha(dataConvertida, false).total);
    }

    /**
     * Gera de fato a folha de pagamento de uma data (persistindo os descontos sindicais
     * calculados) e grava o relatorio no arquivo de saida indicado. Usa folhasGeradas
     * como cache para nao recalcular/remover a divida sindical duas vezes se a mesma
     * data for rodada novamente.
     */
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

    /**
     * Empilha a foto do estado anterior a execucao de um comando que muda dados,
     * habilitando o undo. Qualquer novo comando invalida a pilha de redo (nao faz
     * mais sentido refazer algo depois que um novo comando foi executado).
     */
    private void registrarComando(byte[] fotoAntes) {
        pilhaUndo.push(fotoAntes);
        pilhaRedo.clear();
    }

    /**
     * Desfaz o ultimo comando que alterou o estado do sistema: salva o estado atual
     * na pilha de redo e restaura o estado anterior guardado na pilha de undo.
     */
    public void undo() throws ValidacaoException {
        if (sistemaEncerrado) throw new SistemaEncerradoException();
        if (pilhaUndo.isEmpty()) throw new NaoHaComandoDesfazerException();

        byte[] estadoAtual = tirarFoto();
        byte[] estadoAnterior = pilhaUndo.pop();
        pilhaRedo.push(estadoAtual);
        restaurarFoto(estadoAnterior);
    }

    /**
     * Refaz o ultimo comando desfeito por undo(): salva o estado atual na pilha
     * de undo e restaura o estado seguinte guardado na pilha de redo.
     */
    public void redo() throws ValidacaoException {
        if (sistemaEncerrado) throw new SistemaEncerradoException();
        if (pilhaRedo.isEmpty()) throw new NaoHaComandoRefazerException();

        byte[] estadoAtual = tirarFoto();
        byte[] estadoSeguinte = pilhaRedo.pop();
        pilhaUndo.push(estadoAtual);
        restaurarFoto(estadoSeguinte);
    }




}



