package com.foodordering.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment record linked to an order.
 *
 * <p>Supports simulated UPI, Credit/Debit Cards, Net Banking, and Cash on Delivery.
 */
public class Payment {

    public enum Method  {
        CASH_ON_DELIVERY, DEMO_ONLINE, UPI, CREDIT_DEBIT_CARD, NET_BANKING
    }
    public enum PayStatus { PENDING, COMPLETED, FAILED }

    private int paymentId;
    private int orderId;
    private Method paymentMethod;
    private BigDecimal amount;
    private PayStatus paymentStatus;
    private LocalDateTime paymentDate;
    private String transactionRef;

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Payment() {}

    public Payment(int paymentId, int orderId, Method paymentMethod,
                   BigDecimal amount, PayStatus paymentStatus,
                   LocalDateTime paymentDate) {
        this.paymentId     = paymentId;
        this.orderId       = orderId;
        this.paymentMethod = paymentMethod;
        this.amount        = amount;
        this.paymentStatus = paymentStatus;
        this.paymentDate   = paymentDate;
    }

    public Payment(int paymentId, int orderId, Method paymentMethod,
                   BigDecimal amount, PayStatus paymentStatus,
                   LocalDateTime paymentDate, String transactionRef) {
        this(paymentId, orderId, paymentMethod, amount, paymentStatus, paymentDate);
        this.transactionRef = transactionRef;
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public int getPaymentId()                         { return paymentId;            }
    public void setPaymentId(int id)                  { this.paymentId = id;         }

    public int getOrderId()                           { return orderId;              }
    public void setOrderId(int id)                    { this.orderId = id;           }

    public Method getPaymentMethod()                  { return paymentMethod;        }
    public void setPaymentMethod(Method m)            { this.paymentMethod = m;      }

    public BigDecimal getAmount()                     { return amount;               }
    public void setAmount(BigDecimal amount)          { this.amount = amount;        }

    public PayStatus getPaymentStatus()               { return paymentStatus;        }
    public void setPaymentStatus(PayStatus s)         { this.paymentStatus = s;      }

    public LocalDateTime getPaymentDate()             { return paymentDate;          }
    public void setPaymentDate(LocalDateTime dt)      { this.paymentDate = dt;       }

    public String getTransactionRef()                 { return transactionRef;       }
    public void setTransactionRef(String ref)         { this.transactionRef = ref;   }

    @Override
    public String toString() {
        return "Payment{orderId=" + orderId + ", method=" + paymentMethod
               + ", status=" + paymentStatus + ", amount=" + amount + '}';
    }
}
