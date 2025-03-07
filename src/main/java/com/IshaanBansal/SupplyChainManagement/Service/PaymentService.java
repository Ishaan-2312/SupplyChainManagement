package com.IshaanBansal.SupplyChainManagement.Service;

import com.razorpay.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
//import org.web3j.abi.datatypes.Uint256;
import org.web3j.abi.datatypes.Utf8String;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Service
public class PaymentService {

    private final RazorpayClient razorpay;
    private final Web3jPaymentService web3jPaymentService;

    private String razorpayKeyId="rzp_test_xgz9aDl9YkIYBO";

    private String razorpayKeySecret="3fTxATVwIe8smK30543Q9adZ";


    public PaymentService(Web3jPaymentService web3jPaymentService) throws RazorpayException {
        this.razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret); // ❌ ERROR: Null values
        this.web3jPaymentService = web3jPaymentService;
    }
    Logger log= LoggerFactory.getLogger(PaymentService.class);


    // ✅ Step 1: Create a Razorpay Order (Initiate Payment)
    public String initiatePayment(Long orderId, BigInteger amount) throws Exception {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount.multiply(BigInteger.valueOf(100))); // Convert to paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_" + orderId);
        orderRequest.put("payment_capture", 1); // Auto-capture enabled

        Order order = razorpay.Orders.create(orderRequest);
        return order.toString(); //Return Razorpay Order
    }


    public String processPayment(Long orderId) throws Exception {
        String razorpayOrderId = "order_" + orderId;

        JSONObject options = new JSONObject();
        options.put("count", 10);
        options.put("skip", 0);

        // Fetch all payments as a List
        List<Payment> paymentsList = razorpay.Payments.fetchAll(options);

        log.info(paymentsList + " This is the LIST");

        for (Payment payment : paymentsList) {
            JSONObject paymentJson = new JSONObject(payment.toString()); // Convert Payment to JSON

            if (paymentJson.has("order_id") && paymentJson.getString("order_id").equals(razorpayOrderId)) {
                return paymentJson.getString("id"); // Return Payment ID
            }
        }

        throw new RuntimeException("❌ No successful payment found for Order ID: " + orderId);
    }




    public String getExistingOrderPaymentLink(Long orderId) throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Fetch recent orders from Razorpay
        JSONObject fetchRequest = new JSONObject();
        fetchRequest.put("count", 10);
        fetchRequest.put("skip", 0);

        // Fetch all orders and store them in a List
        List<Order> orders = razorpay.Orders.fetchAll(fetchRequest);
        log.info(orders+"ORDERS");

        for (Order order : orders) {
            String receipt = order.get("receipt");

            if (receipt != null && receipt.equals("order_" + orderId)) {
                return "https://rzp.io/l/" + order.get("id"); // ✅ Return payment link
            }
        }

        throw new RuntimeException("❌ No matching order found for Order ID: " + orderId);
    }


    // ✅ Step 3: Verify & Record Payment on Blockchain
    public String recordPayment(Long orderId, String payeeAddress) throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Fetch payments related to this order
        JSONObject fetchRequest = new JSONObject();
        fetchRequest.put("count", 10);
        fetchRequest.put("skip", 0);

        List<Payment> payments = razorpay.Payments.fetchAll(fetchRequest);

        for (Payment payment : payments) {
            if (payment.get("status").equals("captured")) {
                BigInteger amount = new BigInteger(payment.get("amount").toString());
                String razorpayPaymentId = payment.get("id");

                // ✅ Store the payment on Blockchain
                return web3jPaymentService.recordRazorpayPayment(orderId, payeeAddress, amount);
            }
        }

        throw new RuntimeException("❌ No successful payment found for Order ID: " + orderId);
    }


    // ✅ Step 4: Withdraw Payment (Refund)
    public String withdrawPayment(Long orderId) throws Exception {
        String razorpayPaymentId = processPayment(orderId);
        Refund refund = razorpay.Payments.refund(razorpayPaymentId);
        return refund.get("id"); // Return Refund ID
    }
}

