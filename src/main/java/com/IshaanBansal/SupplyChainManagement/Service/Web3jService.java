package com.IshaanBansal.SupplyChainManagement.Service;

import com.IshaanBansal.SupplyChainManagement.Model.Order;
import com.IshaanBansal.SupplyChainManagement.Repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Utf8String;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class Web3jService {

    private final Web3j web3j;
    private final Credentials credentials;
    Logger log= LoggerFactory.getLogger(Web3jService.class);
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderService orderService;

    @Value("${ethereum.contractAddress}")
    private String contractAddress;

    //    @Value("${ethereum.privateKey}")
    private String privateKey="";

    public Web3jService() {
        String infuraUrl = "https://sepolia.infura.io/v3/3a699b648b924eaf8f0263bbb862578e";
        this.web3j = Web3j.build(new HttpService(infuraUrl));
        log.info(privateKey);
        this.credentials = Credentials.create(privateKey);
    }

    // Function to place an order on the blockchain
    public String placeOrder(Long orderId, String productName, int quantity, double totalPrice, String supplierAddress) throws Exception {
        Order newOrder=new Order();

        newOrder.setProductName(productName);
        newOrder.setQuantity(quantity);
        newOrder.setTotalPrice(totalPrice);
        newOrder.setSupplierAddress(supplierAddress);
        newOrder.setStatus("PENDING");
        orderRepository.save(newOrder);
        Function function = new Function(
                "placeOrder",
                Arrays.asList(
                        new Uint256(orderId),
                        new Utf8String(productName),
                        new Uint256(quantity),
                        new Uint256(BigInteger.valueOf((long) totalPrice)),
                        new org.web3j.abi.datatypes.Address(supplierAddress)
                ),
                Collections.emptyList()
        );

        return sendTransaction(function);
    }

    // Function to mark an order as dispatched
    public String markAsDispatched(Long orderId) throws Exception {
        // Step 1: Fetch the order from the database
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);

        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        Order order = optionalOrder.get();

        // Step 2: Ensure the order is in the correct state to be dispatched
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Order must be in PENDING state to be dispatched. Current status: " + order.getStatus());
        }

        // Step 3: Call the smart contract function to mark it as dispatched
        Function function = new Function(
                "markAsDispatched",
                Collections.singletonList(new Uint256(orderId)),
                Collections.emptyList()
        );

        String transactionHash = sendTransaction(function);

        // Step 4: Update the status in the database
        order.setStatus("DISPATCHED");
        orderRepository.save(order);

        return transactionHash;
    }


    // Function to mark an order as received
    public String markAsReceived(Long orderId) throws Exception {
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);

        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        Order order = optionalOrder.get();

        // Step 2: Ensure the order is in the correct state to be dispatched
        if (!"DISPATCHED".equals(order.getStatus())) {
            throw new RuntimeException("Order must be in DISPATCHED state to be RECEIVED. Current status: " + order.getStatus());
        }

        // Step 3: Call the smart contract function to mark it as dispatched
        Function function = new Function(
                "markAsReceived",
                Collections.singletonList(new Uint256(orderId)),
                Collections.emptyList()
        );

        String transactionHash = sendTransaction(function);

        // Step 4: Update the status in the database
        order.setStatus("RECEIVED");
        orderRepository.save(order);

        return transactionHash;
    }

    // Function to get order details
    public List<Type> getOrder(Long orderId) throws Exception {
        Function function = new Function(
                "getOrder",
                Collections.singletonList(new Uint256(orderId)),
                Arrays.asList(
                        new TypeReference<Uint256>() {},  // Order ID
                        new TypeReference<Utf8String>() {}, // Product Name
                        new TypeReference<Uint256>() {},  // Quantity
                        new TypeReference<Uint256>() {},  // Total Price
                        new TypeReference<Uint256>() {},  // Status
                        new TypeReference<Uint256>() {},  // Timestamp
                        new TypeReference<org.web3j.abi.datatypes.Address>() {}, // Buyer
                        new TypeReference<org.web3j.abi.datatypes.Address>() {}  // Supplier
                )
        );

        return callContractFunction(function);
    }

    // Generic method to send transactions
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


    // Generic method to call smart contract read functions
    private List<Type> callContractFunction(Function function) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
        ).send();

        if (response.isReverted()) {
            throw new RuntimeException("Contract call reverted: " + response.getRevertReason());
        }

        return FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
    }
}


