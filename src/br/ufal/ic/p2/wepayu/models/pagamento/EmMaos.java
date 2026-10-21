package br.ufal.ic.p2.wepayu.models.pagamento;

/**
 * Forma de pagamento "em maos": o empregado retira o cheque pessoalmente.
 * E a forma padrao de todo empregado recem-cadastrado. Nao guarda nenhum dado.
 */
public class EmMaos implements MetodoPagamento {

    @Override
    public String getNome() {
        return "emMaos";
    }

    @Override
    public String descreverParaFolha(String enderecoDoEmpregado) {
        return "Em maos";
    }
}
