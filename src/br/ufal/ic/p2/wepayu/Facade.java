package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.Exception.ValidacaoException;

import java.util.HashMap;
import java.util.Map;

public class Facade {

    private Map<String, Empregado> empregados = new HashMap<>();

    public void zerarSistema() {
        empregados.clear();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws ValidacaoException {
        if (nome == null || nome.isEmpty()) {
            throw new ValidacaoException("Nome nao pode ser nulo.");
        }
        if (endereco == null || endereco.isEmpty()) {
            throw new ValidacaoException("Endereco nao pode ser nulo.");
        }
        if (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new ValidacaoException("Tipo invalido.");
        }
        if (tipo.equals("comissionado")) {
            throw new ValidacaoException("Tipo nao aplicavel.");
        }
        if (salario == null || salario.isEmpty()) {
            throw new ValidacaoException("Salario nao pode ser nulo.");
        }
        double salarioConvertido;
        try {
            salarioConvertido = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Salario deve ser numerico.");
        }
        if (salarioConvertido < 0) {
            throw new ValidacaoException("Salario deve ser nao-negativo.");
        }

        String id = String.valueOf(empregados.size() + 1);
        Empregado empregado = new Empregado(id, nome, endereco, tipo, salarioConvertido);
        empregados.put(id, empregado);
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws ValidacaoException {
        if (nome == null || nome.isEmpty()) {
            throw new ValidacaoException("Nome nao pode ser nulo.");
        }
        if (endereco == null || endereco.isEmpty()) {
            throw new ValidacaoException("Endereco nao pode ser nulo.");
        }
        if (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) {
            throw new ValidacaoException("Tipo invalido.");
        }
        if (!tipo.equals("comissionado")) {
            throw new ValidacaoException("Tipo nao aplicavel.");
        }
        if (salario == null || salario.isEmpty()) {
            throw new ValidacaoException("Salario nao pode ser nulo.");
        }
        double salarioConvertido;
        try {
            salarioConvertido = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Salario deve ser numerico.");
        }
        if (salarioConvertido < 0) {
            throw new ValidacaoException("Salario deve ser nao-negativo.");
        }
        if (comissao == null || comissao.isEmpty()) {
            throw new ValidacaoException("Comissao nao pode ser nula.");
        }
        double comissaoConvertida;
        try {
            comissaoConvertida = Double.parseDouble(comissao.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("Comissao deve ser numerica.");
        }
        if (comissaoConvertida < 0) {
            throw new ValidacaoException("Comissao deve ser nao-negativa.");
        }

        String id = String.valueOf(empregados.size() + 1);
        Empregado empregado = new Empregado(id, nome, endereco, tipo, salarioConvertido);
        empregado.setComissao(comissaoConvertida);
        empregados.put(id, empregado);
        return id;
    }

    public String getAtributoEmpregado(String emp, String atributo) throws ValidacaoException {
        if (emp == null || emp.isEmpty()) {
            throw new ValidacaoException("Identificacao do empregado nao pode ser nula.");
        }
        Empregado empregado = empregados.get(emp);
        if (empregado == null) {
            throw new EmpregadoNaoExisteException();
        }
        switch (atributo) {
            case "nome":
                return empregado.getNome();
            case "endereco":
                return empregado.getEndereco();
            case "tipo":
                return empregado.getTipo();
            case "salario":
                return formataValor(empregado.getSalario());
            case "comissao":
                return formataValor(empregado.getComissao());
            case "sindicalizado":
                return String.valueOf(empregado.isSindicalizado());
            default:
                throw new ValidacaoException("Atributo nao existe.");
        }
    }

    private String formataValor(double valor) {
        return String.format(java.util.Locale.US, "%.2f", valor).replace(".", ",");
    }

    public void encerrarSistema() {
    }
}
