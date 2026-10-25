package br.ufal.ic.p2.wepayu.util;

import br.ufal.ic.p2.wepayu.excecoes.ValidacaoException;

import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Conversao de datas recebidas como texto no formato "d/m/aaaa" (como chegam nos
 * comandos) para LocalDate. Antes, cada comando da Facade repetia o seu proprio
 * try/catch; agora a conversao fica em um unico lugar e cada chamador so informa
 * qual excecao de negocio lancar quando a data for invalida.
 */
public final class Datas {

    private Datas() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    /**
     * Converte o texto em LocalDate. Qualquer problema (texto nulo, formato errado,
     * parte nao numerica ou data que nao existe, como 30/2/2005) vira a excecao
     * produzida por "erro", escolhida por quem chamou (data invalida, data inicial
     * invalida, data final invalida...).
     */
    public static LocalDate converter(String texto, Supplier<? extends ValidacaoException> erro)
            throws ValidacaoException {
        try {
            String[] partes = texto.split("/");
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);
            return LocalDate.of(ano, mes, dia);
        } catch (RuntimeException e) {
            throw erro.get();
        }
    }
}