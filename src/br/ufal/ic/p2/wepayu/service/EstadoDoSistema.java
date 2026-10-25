package br.ufal.ic.p2.wepayu.service;

/**
 * "Foto" do estado do sistema em um instante (padrao Memento). Guarda os dados ja
 * serializados em bytes e e imutavel: quem recebe um EstadoDoSistema (como a Facade)
 * nao consegue olhar dentro dele nem altera-lo, so o devolve ao GerenciadorDeEstado
 * para restaurar o sistema ao que ele era naquele momento.
 *
 * O construtor e o acesso aos bytes sao restritos ao pacote service, onde ficam as
 * classes que criam, restauram e gravam em disco essas fotos.
 */
public final class EstadoDoSistema {
    private final byte[] dados;

    EstadoDoSistema(byte[] dados) {
        this.dados = dados.clone();
    }

    /** Copia dos bytes da foto, para restaurar o sistema ou gravar em disco. */
    byte[] getDados() {
        return dados.clone();
    }
}