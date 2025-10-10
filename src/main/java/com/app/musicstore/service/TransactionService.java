package com.app.musicstore.service;

import com.app.musicstore.model.Transaction;
import com.app.musicstore.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getTransactionsBySellerId(Long sellerId) {
        return transactionRepository.findBySellerId(sellerId);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public Transaction createTransaction(Long sellerId, double amount, String type, String status) {
        Transaction transaction = new Transaction();
        transaction.setSellerId(sellerId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransactionStatus(Long transactionId, String newStatus) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            transaction.setStatus(newStatus);
            return transactionRepository.save(transaction);
        }
        return null;
    }

    public double getTotalEarningsBySellerId(Long sellerId) {
        Double total = transactionRepository.sumBySellerIdAndType(sellerId, "SALE");
        return total != null ? total : 0.0;
    }

    public double getCompletedEarningsBySellerId(Long sellerId) {
        Double total = transactionRepository.sumBySellerIdAndStatus(sellerId, "COMPLETED");
        return total != null ? total : 0.0;
    }

    public double getPendingEarningsBySellerId(Long sellerId) {
        Double total = transactionRepository.sumBySellerIdAndStatus(sellerId, "PENDING");
        return total != null ? total : 0.0;
    }

    public double getTotalRefundsBySellerId(Long sellerId) {
        Double total = transactionRepository.sumBySellerIdAndType(sellerId, "REFUND");
        return total != null ? Math.abs(total) : 0.0; // Refunds are negative, so we take absolute value
    }

    public Transaction createSaleTransaction(Long sellerId, double amount) {
        return createTransaction(sellerId, amount, "SALE", "COMPLETED");
    }

    public Transaction createRefundTransaction(Long sellerId, double amount) {
        return createTransaction(sellerId, -amount, "REFUND", "COMPLETED"); // Refunds are negative
    }

    public Transaction createWithdrawalRequest(Long sellerId, double amount) {
        return createTransaction(sellerId, -amount, "WITHDRAWAL", "PENDING"); // Withdrawals are negative
    }

    public void deleteTransaction(Long transactionId) {
        transactionRepository.deleteById(transactionId);
    }

    public List<Transaction> getTransactionsByType(Long sellerId, String type) {
        return transactionRepository.findBySellerId(sellerId).stream()
                .filter(t -> t.getType().equals(type))
                .toList();
    }

    public List<Transaction> getTransactionsByStatus(Long sellerId, String status) {
        return transactionRepository.findBySellerId(sellerId).stream()
                .filter(t -> t.getStatus().equals(status))
                .toList();
    }
}

