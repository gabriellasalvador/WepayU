package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.Exception.cadastro.*;
import br.ufal.ic.p2.wepayu.Exception.lancamentos.*;
import br.ufal.ic.p2.wepayu.Exception.pagamento.AgenciaNulaException;
import br.ufal.ic.p2.wepayu.Exception.pagamento.BancoNuloException;
import br.ufal.ic.p2.wepayu.Exception.pagamento.ContaCorrenteNulaException;
import br.ufal.ic.p2.wepayu.Exception.sindicato.*;
import br.ufal.ic.p2.wepayu.Exception.sistema.SistemaEncerradoException;
import br.ufal.ic.p2.wepayu.Exception.tipoempregado.*;
import br.ufal.ic.p2.wepayu.models.CartaoDePonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.TipoEmpregado;
import br.ufal.ic.p2.wepayu.models.EmpregadoAssalariado;
import br.ufal.ic.p2.wepayu.models.EmpregadoComissionado;
import br.ufal.ic.p2.wepayu.models.EmpregadoHorista;
import br.ufal.ic.p2.wepayu.models.ResultadoVenda;
import br.ufal.ic.p2.wepayu.models.TaxaServico;
import br.ufal.ic.p2.wepayu.models.pagamento.Banco;
import br.ufal.ic.p2.wepayu.models.pagamento.FabricaMetodoPagamento;
import br.ufal.ic.p2.wepayu.models.sindicato.Filiado;
import br.ufal.ic.p2.wepayu.models.sindicato.NaoFiliado;
import br.ufal.ic.p2.wepayu.repository.RepositorioEmpregados;
import br.ufal.ic.p2.wepayu.service.ArmazenamentoDeEstado;
import br.ufal.ic.p2.wepayu.service.EstadoDoSistema;
import br.ufal.ic.p2.wepayu.service.FolhaDePagamento;
import br.ufal.ic.p2.wepayu.service.GerenciadorDeEstado;
import br.ufal.ic.p2.wepayu.util.Formatacao;


import java.time.LocalDate;
import java.util.List;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;

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

    // Repositorio unico (Singleton) onde ficam os empregados e o contador de ids;
    // a Facade nao guarda empregados por conta propria, sempre pergunta a ele.
    private final RepositorioEmpregados repositorio = RepositorioEmpregados.getInstancia();
    // Servico que calcula a folha de pagamento e monta o relatorio.
    private final FolhaDePagamento folha = new FolhaDePagamento(repositorio);
    // Cuida das fotos do estado: undo/redo (Memento) e restauracao do estado salvo.
    private final GerenciadorDeEstado gerenciador = new GerenciadorDeEstado(repositorio, folha);
    // Grava e le em disco o arquivo onde o estado do sistema e persistido ao encerrar.
    private final ArmazenamentoDeEstado armazenamento = new ArmazenamentoDeEstado("estado.dat");
    // Marca se encerrarSistema() ja foi chamado; depois disso nenhum outro comando pode rodar.
    private boolean sistemaEncerrado = false;

    /**
     * Ao criar a fachada, tenta restaurar o estado salvo por um encerrarSistema()
     * anterior (arquivo estado.dat), simulando persistencia entre execucoes.
     */
    public Facade() {
        armazenamento.carregar().ifPresent(gerenciador::restaurar);
    }


    /**
     * Reseta completamente o sistema (empregados, folhas geradas e contador de id),
     * registrando o estado anterior na pilha de undo.
     */
    public void zerarSistema() throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        repositorio.zerar();
        folha.limparFolhasGeradas();
        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Encerra o sistema: bloqueia qualquer comando futuro (inclusive undo/redo) e
     * grava o estado atual em disco para poder ser recarregado numa proxima execucao.
     */
    public void encerrarSistema() {
        sistemaEncerrado = true;
        armazenamento.salvar(gerenciador.tirarFoto());
    }

    /**
     * Garante que o sistema ainda aceita comandos. Depois de encerrarSistema()
     * nenhum comando (nem consulta, nem undo/redo) pode ser executado. Todo metodo
     * publico de comando chama este guard na primeira linha, antes de tocar no estado.
     */
    private void verificarSistemaAtivo() throws SistemaEncerradoException {
        if (sistemaEncerrado) throw new SistemaEncerradoException();
    }

    /**
     * Converte um texto numerico (aceitando virgula ou ponto decimal) para double.
     * Se o texto for nulo ou invalido, lanca a excecao de negocio fornecida pelo
     * chamador, em vez de deixar escapar um NumberFormatException sem tratamento.
     * Centraliza o que antes era repetido em cada comando que recebe numeros.
     */
    private double converterParaDouble(String texto, Supplier<? extends ValidacaoException> erro) throws ValidacaoException {
        if (texto == null) throw erro.get();
        try {
            return Double.parseDouble(texto.replace(",", "."));
        } catch (NumberFormatException e) {
            throw erro.get();
        }
    }

    /**
     * Garante que um texto obrigatorio foi informado (nao nulo e nao vazio), lancando
     * a excecao indicada em caso contrario. Evita repetir o mesmo "if" em cada comando.
     */
    private void exigirPreenchido(String valor, Supplier<? extends ValidacaoException> erro) throws ValidacaoException {
        if (valor == null || valor.isEmpty()) throw erro.get();
    }

    /**
     * Valida e converte um salario recebido como texto: obrigatorio, numerico e nao
     * negativo. Usado ao criar empregados e ao alterar o salario.
     */
    private double converterSalario(String salario) throws ValidacaoException {
        exigirPreenchido(salario, SalarioNuloException::new);
        double convertido = converterParaDouble(salario, SalarioNaoNumericoException::new);
        if (convertido < 0) throw new SalarioNegativoException();
        return convertido;
    }

    /**
     * Valida e converte uma comissao recebida como texto: obrigatoria, numerica e nao
     * negativa. Usado ao criar comissionados e ao alterar a comissao.
     */
    private double converterComissao(String comissao) throws ValidacaoException {
        exigirPreenchido(comissao, ComissaoNulaException::new);
        double convertida = converterParaDouble(comissao, ComissaoNaoNumericaException::new);
        if (convertida < 0) throw new ComissaoNegativaException();
        return convertida;
    }

    /**
     * Gera o proximo id, cria o empregado pela fabrica do tipo (TipoEmpregado) e o
     * registra no sistema, empilhando o estado anterior para o undo.
     */
    private String cadastrar(TipoEmpregado tipo, String nome, String endereco,
                             double salario, double comissao, EstadoDoSistema fotoAntes) {
        String id = repositorio.gerarProximoId();
        repositorio.salvar(tipo.criar(id, nome, endereco, salario, comissao));
        gerenciador.registrarComando(fotoAntes);
        return id;
    }

    /**
     * Cria um empregado de um tipo que nao tem comissao (horista ou assalariado).
     * Valida nome, endereco, tipo e salario antes de persistir o novo empregado.
     */
    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        exigirPreenchido(nome, NomeNuloException::new);
        exigirPreenchido(endereco, EnderecoNuloException::new);
        TipoEmpregado tipoEmpregado = TipoEmpregado.porNome(tipo);
        if (tipoEmpregado.exigeComissao()) throw new TipoNaoAplicavelException();
        double salarioConvertido = converterSalario(salario);

        return cadastrar(tipoEmpregado, nome, endereco, salarioConvertido, 0, fotoAntes);
    }

    /**
     * Sobrecarga usada para criar empregados de um tipo que exige comissao (o
     * comissionado), que informam tambem o valor da comissao alem do salario fixo.
     */
    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        exigirPreenchido(nome, NomeNuloException::new);
        exigirPreenchido(endereco, EnderecoNuloException::new);
        TipoEmpregado tipoEmpregado = TipoEmpregado.porNome(tipo);
        if (!tipoEmpregado.exigeComissao()) throw new TipoNaoAplicavelException();
        double salarioConvertido = converterSalario(salario);
        double comissaoConvertida = converterComissao(comissao);

        return cadastrar(tipoEmpregado, nome, endereco, salarioConvertido, comissaoConvertida, fotoAntes);
    }

    /**
     * Remove um empregado existente do sistema pelo id.
     */
    public void removerEmpregado(String emp) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        Empregado empregado = buscarEmpregado(emp);
        gerenciador.registrarComando(fotoAntes);
        repositorio.remover(empregado.getId());
    }

    /**
     * Le um atributo qualquer de um empregado a partir do nome do atributo (string),
     * despachando para o getter correspondente e validando pre-condicoes especificas
     * (ex.: "comissao" so faz sentido para comissionados, "banco" so para pagamento em banco).
     */
    public String getAtributoEmpregado(String emp, String atributo) throws ValidacaoException {
        verificarSistemaAtivo();
        Empregado empregado = buscarEmpregado(emp);

        switch (atributo) {
            case "nome": return empregado.getNome();
            case "endereco": return empregado.getEndereco();
            case "tipo": return empregado.getTipo().getNome();
            case "salario": return Formatacao.valor(empregado.getSalario());
            // Comissao: quem nao e comissionado lanca EmpregadoNaoEhComissionadoException sozinho.
            case "comissao":
                return Formatacao.valor(empregado.getComissao());
            case "sindicalizado": return String.valueOf(empregado.getSindicato().isFiliado());
            case "metodoPagamento":
                return empregado.getMetodoPagamento().getNome();
            // Banco, agencia e conta: quem decide se o empregado recebe em banco e a propria
            // forma de pagamento (so Banco responde; as outras lancam EmpregadoNaoRecebeEmBancoException).
            case "banco":
                return empregado.getMetodoPagamento().getBanco();
            case "agencia":
                return empregado.getMetodoPagamento().getAgencia();
            case "contaCorrente":
                return empregado.getMetodoPagamento().getContaCorrente();
            // Id e taxa sindical: se o empregado nao for filiado, o proprio sindicato
            // lanca EmpregadoNaoEhSindicalizadoException.
            case "idSindicato":
                return empregado.getSindicato().getIdSindicato();
            case "taxaSindical":
                return Formatacao.valor(empregado.getSindicato().getTaxaSindical());
            default: throw new AtributoNaoExisteException();
        }
    }

    /**
     * Retorna quantos empregados estao atualmente cadastrados no sistema.
     */
    public int getNumeroDeEmpregados() throws ValidacaoException {
        verificarSistemaAtivo();
        return repositorio.quantidade();
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
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        Empregado empregado = buscarEmpregado(emp);

        LocalDate dataConvertida;
        try {
            dataConvertida = parseData(data);
        } catch (Exception e) {
            throw new DataInvalidaException();
        }
        double horasConvertidas = converterParaDouble(horas, HorasNaoNumericasException::new);
        if (horasConvertidas <= 0) throw new HorasNaoPositivasException();

        empregado.addCartao(new CartaoDePonto(dataConvertida, horasConvertidas));
        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Busca o id do n-esimo (indice, 1-based) empregado cujo nome contem a substring informada,
     * ordenando os resultados por id (numerico) antes de aplicar o indice.
     */
    public String getEmpregadoPorNome(String nome, String indice) throws ValidacaoException {
        verificarSistemaAtivo();
        int indiceConvertido;
        try {
            indiceConvertido = Integer.parseInt(indice);
        } catch (NumberFormatException e) {
            // Um indice que nem e numero nao aponta para nenhum empregado.
            throw new NaoHaEmpregadoComEsseNomeException();
        }

        List<Empregado> encontrados = new ArrayList<>();
        for (Empregado e : repositorio.listar()) {
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
        verificarSistemaAtivo();
        Empregado empregado = buscarEmpregado(emp);
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
        return Formatacao.horas(total);
    }

    /**
     * Soma as horas extras (acima de 8h por cartao) trabalhadas por um empregado
     * dentro do periodo [dataInicial, dataFinal).
     */
    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws ValidacaoException {
        verificarSistemaAtivo();
        Empregado empregado = buscarEmpregado(emp);
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
        return Formatacao.horas(total);
    }

    /**
     * Lanca uma venda realizada por um empregado comissionado em uma data especifica.
     */
    public void lancaVenda(String emp, String data, String valor) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        Empregado empregado = buscarEmpregado(emp);

        LocalDate dataConvertida;
        try {
            dataConvertida = parseData(data);
        } catch (Exception e) {
            throw new DataInvalidaException();
        }
        double valorConvertido = converterParaDouble(valor, ValorNaoNumericoException::new);
        if (valorConvertido <= 0) throw new ValorNaoPositivoException();

        empregado.addVenda(new ResultadoVenda(dataConvertida, valorConvertido));
        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Soma o valor das vendas realizadas por um empregado comissionado
     * dentro do periodo [dataInicial, dataFinal).
     */
    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws ValidacaoException {
        verificarSistemaAtivo();
        Empregado empregado = buscarEmpregado(emp);
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
        return Formatacao.valor(total);
    }

    /**
     * Localiza o empregado sindicalizado cujo idSindicato corresponde ao "membro" informado.
     * Usado pelos comandos relacionados a taxa de servico, que identificam o empregado
     * pelo id de sindicato e nao pelo id do empregado.
     */
    private Empregado buscarPorIdSindicato(String membro) throws ValidacaoException {
        for (Empregado empregado : repositorio.listar()) {
            if (empregado.getSindicato().isFiliado() && membro.equals(empregado.getSindicato().getIdSindicato())) {
                return empregado;
            }
        }
        throw new MembroNaoExisteException();
    }

    /**
     * Lanca uma taxa de servico cobrada por um empregado sindicalizado, identificado
     * pelo id de sindicato ("membro"), em uma data especifica.
     */
    public void lancaTaxaServico(String membro, String data, String valor) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        if (membro == null || membro.isEmpty()) throw new IdentificacaoMembroNulaException();
        Empregado empregado = buscarPorIdSindicato(membro);

        LocalDate dataConvertida;
        try {
            dataConvertida = parseData(data);
        } catch (Exception e) {
            throw new DataInvalidaException();
        }
        double valorConvertido = converterParaDouble(valor, ValorNaoNumericoException::new);
        if (valorConvertido <= 0) throw new ValorNaoPositivoException();

        empregado.getSindicato().registrarTaxaServico(new TaxaServico(dataConvertida, valorConvertido));
        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Soma o valor das taxas de servico lancadas para um empregado sindicalizado
     * dentro do periodo [dataInicial, dataFinal).
     */
    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws ValidacaoException {
        verificarSistemaAtivo();
        Empregado empregado = buscarEmpregado(emp);
        // Lanca EmpregadoNaoEhSindicalizadoException se nao for filiado (antes de validar as datas).
        List<TaxaServico> taxas = empregado.getSindicato().getTaxasServico();

        LocalDate inicio;
        try { inicio = parseData(dataInicial); } catch (Exception e) { throw new DataInicialInvalidaException(); }
        LocalDate fim;
        try { fim = parseData(dataFinal); } catch (Exception e) { throw new DataFinalInvalidaException(); }
        if (inicio.isAfter(fim)) throw new DataInicialPosteriorException();

        double total = 0;
        for (TaxaServico taxa : taxas) {
            LocalDate data = taxa.getData();
            if (!data.isBefore(inicio) && data.isBefore(fim)) {
                total += taxa.getValor();
            }
        }
        return Formatacao.valor(total);
    }

    /**
     * Helper comum para localizar um empregado por id, lancando as excecoes
     * de validacao padrao (id nulo/vazio, empregado inexistente).
     */
    private Empregado buscarEmpregado(String emp) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) throw new IdentificacaoNulaException();
        return repositorio.buscarPorId(emp).orElseThrow(EmpregadoNaoExisteException::new);
    }

    /**
     * Troca o tipo de um empregado, criando pela fabrica (TipoEmpregado) uma nova
     * instancia da classe correta (mantendo o mesmo id) e copiando para ela o estado
     * comum do empregado antigo (nome, endereco, sindicalizacao, forma de pagamento,
     * etc). Permite opcionalmente definir salario/comissao no momento da troca.
     */
    private void trocarTipo(Empregado antigo, String novoTipo, String novoSalario, String novaComissao) throws ValidacaoException {
        TipoEmpregado tipo = TipoEmpregado.porNome(novoTipo);

        double salarioFinal = antigo.getSalario();
        if (novoSalario != null) {
            salarioFinal = converterParaDouble(novoSalario, SalarioNaoNumericoException::new);
        }
        double comissaoFinal = 0;
        if (novaComissao != null) {
            comissaoFinal = converterParaDouble(novaComissao, ComissaoNaoNumericaException::new);
        }

        Empregado novo = tipo.criar(antigo.getId(), antigo.getNome(), antigo.getEndereco(), salarioFinal, comissaoFinal);
        novo.copiarEstadoComumDe(antigo);
        repositorio.salvar(novo);
    }

    /**
     * Altera um atributo simples de um empregado (nome, endereco, salario, comissao,
     * tipo, metodoPagamento ou sindicalizado=false), validando o novo valor conforme
     * o atributo escolhido.
     */
    public void alteraEmpregado(String emp, String atributo, String valor) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        Empregado empregado = buscarEmpregado(emp);

        switch (atributo) {
            case "nome":
                exigirPreenchido(valor, NomeNuloException::new);
                empregado.setNome(valor);
                break;
            case "endereco":
                exigirPreenchido(valor, EnderecoNuloException::new);
                empregado.setEndereco(valor);
                break;
            case "salario":
                empregado.setSalario(converterSalario(valor));
                break;
            case "comissao":
                empregado.setComissao(converterComissao(valor));
                break;
            case "tipo":
                trocarTipo(empregado, valor, null, null);
                break;
            case "metodoPagamento":
                // A fabrica escolhe a classe certa (EmMaos ou Correios) ou lanca MetodoPagamentoInvalidoException.
                empregado.setMetodoPagamento(FabricaMetodoPagamento.criarSemDadosExtras(valor));
                break;
            case "sindicalizado":
                if (valor.equals("false")) {
                    empregado.setSindicato(new NaoFiliado());
                } else {
                    throw new ValorSindicalizadoInvalidoException();
                }
                break;
            default:
                throw new AtributoNaoExisteException();
        }

        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Sobrecarga de alteraEmpregado usada especificamente para trocar o tipo do
     * empregado ja informando, no mesmo comando, o novo salario (para horista)
     * ou a nova comissao (para comissionado).
     */
    public void alteraEmpregado(String emp, String atributo, String valor, String valorExtra) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        Empregado empregado = buscarEmpregado(emp);
        if (!atributo.equals("tipo")) throw new AtributoNaoExisteException();

        // O proprio tipo informa que dado extra aceita na troca: salario, comissao ou nenhum.
        TipoEmpregado tipo = TipoEmpregado.porNome(valor);
        if (tipo.exigeComissao()) {
            trocarTipo(empregado, valor, null, valorExtra);
        } else if (tipo.trocaComNovoSalario()) {
            trocarTipo(empregado, valor, valorExtra, null);
        } else {
            throw new TipoInvalidoException();
        }
        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Sobrecarga de alteraEmpregado usada para sindicalizar um empregado, recebendo
     * o idSindicato (que deve ser unico entre os empregados sindicalizados) e a taxa
     * sindical a ser cobrada.
     */
    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        Empregado empregado = buscarEmpregado(emp);

        if (idSindicato == null || idSindicato.isEmpty()) throw new IdentificacaoSindicatoNulaException();

        for (Empregado outro : repositorio.listar()) {
            if (outro != empregado && outro.getSindicato().isFiliado() && idSindicato.equals(outro.getSindicato().getIdSindicato())) {
                throw new IdentificacaoSindicatoDuplicadaException();
            }
        }

        if (taxaSindical == null || taxaSindical.isEmpty()) throw new TaxaSindicalNulaException();
        double taxaConvertida = converterParaDouble(taxaSindical, TaxaSindicalNaoNumericaException::new);
        if (taxaConvertida < 0) throw new TaxaSindicalNegativaException();

        empregado.setSindicato(new Filiado(idSindicato, taxaConvertida));
        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Sobrecarga de alteraEmpregado usada para configurar o pagamento em banco,
     * recebendo banco, agencia e conta corrente.
     */
    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        Empregado empregado = buscarEmpregado(emp);

        if (banco == null || banco.isEmpty()) throw new BancoNuloException();
        if (agencia == null || agencia.isEmpty()) throw new AgenciaNulaException();
        if (contaCorrente == null || contaCorrente.isEmpty()) throw new ContaCorrenteNulaException();

        empregado.setMetodoPagamento(new Banco(banco, agencia, contaCorrente));
        gerenciador.registrarComando(fotoAntes);
    }

// ===================== FOLHA DE PAGAMENTO (US7) =====================

    /**
     * Retorna apenas o valor total da folha para uma data, sem gerar o relatorio
     * em arquivo e sem persistir descontos sindicais (calculo "de previa").
     */
    public String totalFolha(String data) throws ValidacaoException {
        verificarSistemaAtivo();
        LocalDate dataConvertida;
        try { dataConvertida = parseData(data); } catch (Exception e) { throw new DataInvalidaException(); }
        return Formatacao.valor(folha.calcular(dataConvertida, false).getTotal());
    }

    /**
     * Gera de fato a folha de pagamento de uma data (persistindo os descontos sindicais
     * calculados) e grava o relatorio no arquivo de saida indicado. O servico de folha
     * guarda as folhas ja geradas, para nao remover a divida sindical duas vezes se a
     * mesma data for rodada novamente.
     */
    public void rodaFolha(String data, String saida) throws ValidacaoException {
        verificarSistemaAtivo();
        EstadoDoSistema fotoAntes = gerenciador.tirarFoto();
        LocalDate dataConvertida;
        try { dataConvertida = parseData(data); } catch (Exception e) { throw new DataInvalidaException(); }

        String relatorio = folha.gerarRelatorio(dataConvertida);

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(saida), "UTF-8"))) {
            out.print(relatorio);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever arquivo da folha: " + e.getMessage(), e);
        }
        gerenciador.registrarComando(fotoAntes);
    }

    /**
     * Desfaz o ultimo comando que alterou o estado do sistema (quem restaura o estado
     * anterior e o GerenciadorDeEstado).
     */
    public void undo() throws ValidacaoException {
        verificarSistemaAtivo();
        gerenciador.desfazer();
    }

    /**
     * Refaz o ultimo comando desfeito por undo().
     */
    public void redo() throws ValidacaoException {
        verificarSistemaAtivo();
        gerenciador.refazer();
    }

}