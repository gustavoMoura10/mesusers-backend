package com.br.mesusers.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponse(
        @JsonProperty("cep") String cep,
        @JsonProperty("logradouro") String street,
        @JsonProperty("complemento") String complement,
        @JsonProperty("bairro") String neighborhood,
        @JsonProperty("localidade") String city,
        @JsonProperty("uf") String state,
        @JsonProperty("erro") boolean error) {

    public ViaCepResponse(String cep, String logradouro, String complemento, String bairro, String localidade,
            String uf) {
        this(cep, logradouro, complemento, bairro, localidade, uf, false);
    }

}