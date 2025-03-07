package com.IshaanBansal.SupplyChainManagement.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

    private static final String INFURA_URL = "https://sepolia.infura.io/v3/3a699b648b924eaf8f0263bbb862578e";

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(INFURA_URL));
    }
}


