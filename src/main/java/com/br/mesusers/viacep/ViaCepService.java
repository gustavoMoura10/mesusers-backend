package com.br.mesusers.viacep;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Service
public class ViaCepService {

    private final RestTemplate restTemplate;

    public ViaCepService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ViaCepResponse getAddressFromCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep.replace("-", "") + "/json/";
        var response = restTemplate.getForObject(url, ViaCepResponse.class);

        if (response == null) {
            throw new RuntimeException("CEP não encontrado");
        }
        return response;
    }

}
