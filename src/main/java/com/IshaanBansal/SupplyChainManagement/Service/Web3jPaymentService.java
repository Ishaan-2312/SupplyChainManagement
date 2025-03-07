package com.IshaanBansal.SupplyChainManagement.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

@Service
public class Web3jPaymentService {

    private final Web3j web3j;
    private final Credentials credentials;

    @Value("${ethereum.contractAddress}")
    private String contractAddress;

    public Web3jPaymentService(@Value("${ethereum.privateKey}") String privateKey) {
        this.web3j = Web3j.build(new org.web3j.protocol.http.HttpService("https://sepolia.infura.io/v3/YOUR_INFURA_PROJECT_ID"));
        this.credentials = Credentials.create(privateKey);
    }

    // ✅ Step 3: Record Payment on Blockchain
    public String recordRazorpayPayment(Long orderId, String payeeAddress, BigInteger amount) throws Exception {
        Function function = new Function(
                "recordPayment",
                Arrays.asList(new Uint256(orderId), new Address(payeeAddress), new Uint256(amount)),
                Collections.emptyList()
        );

        return sendTransaction(function);
    }

    // ✅ Step 4: Withdraw Payment (Blockchain)
    public String withdrawPayment(Long orderId, String payeeAddress) throws Exception {
        Function function = new Function(
                "withdrawPayment",
                Arrays.asList(new Uint256(orderId), new Address(payeeAddress)),
                Collections.emptyList()
        );

        return sendTransaction(function);
    }

    private String sendTransaction(Function function) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        // Step 1: Fetch the latest nonce for the sender's address
        BigInteger nonce = web3j.ethGetTransactionCount(
                credentials.getAddress(),
                DefaultBlockParameterName.LATEST
        ).send().getTransactionCount();

        // Step 2: Increase the gas price to avoid "replacement transaction underpriced"
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(BigInteger.valueOf(2));

        // Step 3: Set gas limit (adjust if needed)
        BigInteger gasLimit = BigInteger.valueOf(300000); // Adjust based on contract function complexity

        // Step 4: Create the transaction
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, contractAddress, encodedFunction
        );

        // Step 5: Sign the transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        // Step 6: Send the signed transaction
        EthSendTransaction transactionResponse = web3j.ethSendRawTransaction(hexValue).send();

        // Step 7: Handle errors
        if (transactionResponse.getError() != null) {
            throw new RuntimeException("Transaction Error: " + transactionResponse.getError().getMessage());
        }

        return transactionResponse.getTransactionHash();
    }

}


