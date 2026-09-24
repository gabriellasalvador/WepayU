package br.ufal.ic.p2.wepayu.Exception;

/**
 * Superclasse comum de todas as excecoes de validacao/negocio do WePayU
 * Fica na raiz do pacote Exception, fora das subpastas, justamente por ser a base compartilhada
 * por todas elas; e por isso que a Facade declara "throws ValidacaoException"
 * na maioria dos seus metodos, em vez de listar cada excecao especifica.
 */
public class ValidacaoException extends Exception {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}