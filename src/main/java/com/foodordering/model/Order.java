package com.foodordering.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order.
 *
 * <p>Contains a list of {@link OrderItem}s, delivery information, and aggregated monetary totals.
 *
 * <p>The order status follows a state machine enforced in the service layer:
 * PLACED → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED
 *                                                    → CANCELLED (from most states)
 */
public class Order {

    /** Order status constants — state machine. */
    public enum Status {
        PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }

    private int orderId;
    private int customerId;
    private LocalDateTime orderDate;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private Status orderStatus;
    private String deliveryAddress;
    private String contactPhone;
    private String paymentMethod;
    private List<OrderItem> orderItems = new ArrayList<>();

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Order() {}

    public Order(int orderId, int customerId, LocalDateTime orderDate,
                 BigDecimal subtotal, BigDecimal discountAmount,
                 BigDecimal finalAmount, Status orderStatus) {
        this.orderId        = orderId;
        this.customerId     = customerId;
        this.orderDate      = orderDate;
        this.subtotal       = subtotal;
        this.discountAmount = discountAmount;
        this.finalAmount    = finalAmount;
        this.orderStatus    = orderStatus;
    }

    public Order(int orderId, int customerId, LocalDateTime orderDate,
                 BigDecimal subtotal, BigDecimal discountAmount,
                 BigDecimal finalAmount, Status orderStatus,
                 String deliveryAddress, String contactPhone) {
        this(orderId, customerId, orderDate, subtotal, discountAmount, finalAmount, orderStatus);
        this.deliveryAddress = deliveryAddress;
        this.contactPhone    = contactPhone;
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public int getOrderId()                         { return orderId;           }
    public void setOrderId(int id)                  { this.orderId = id;        }

    public int getCustomerId()                      { return customerId;        }
    public void setCustomerId(int id)               { this.customerId = id;     }

    public LocalDateTime getOrderDate()             { return orderDate;         }
    public void setOrderDate(LocalDateTime dt)      { this.orderDate = dt;      }

    public BigDecimal getSubtotal()                 { return subtotal;          }
    public void setSubtotal(BigDecimal v)           { this.subtotal = v;        }

    public BigDecimal getDiscountAmount()           { return discountAmount;    }
    public void setDiscountAmount(BigDecimal v)     { this.discountAmount = v;  }

    public BigDecimal getFinalAmount()              { return finalAmount;       }
    public void setFinalAmount(BigDecimal v)        { this.finalAmount = v;     }

    public Status getOrderStatus()                  { return orderStatus;       }
    public void setOrderStatus(Status s)            { this.orderStatus = s;     }

    public String getDeliveryAddress()              { return deliveryAddress;   }
    public void setDeliveryAddress(String address)  { this.deliveryAddress = address; }

    public String getContactPhone()                 { return contactPhone;      }
    public void setContactPhone(String phone)       { this.contactPhone = phone; }

    public String getPaymentMethod()                { return paymentMethod;     }
    public void setPaymentMethod(String method)     { this.paymentMethod = method; }

    public List<OrderItem> getOrderItems()          { return orderItems;        }
    public void setOrderItems(List<OrderItem> l)    { this.orderItems = l;      }

    // ------------------------------------------------------------------ //
    //  State-machine: valid transitions
    // ------------------------------------------------------------------ //

    /**
     * Returns {@code true} if transitioning to {@code next} is valid.
     */
    public boolean canTransitionTo(Status next) {
        return switch (orderStatus) {
            case PLACED          -> next == Status.CONFIRMED     || next == Status.CANCELLED;
            case CONFIRMED       -> next == Status.PREPARING     || next == Status.CANCELLED;
            case PREPARING       -> next == Status.OUT_FOR_DELIVERY || next == Status.CANCELLED;
            case OUT_FOR_DELIVERY-> next == Status.DELIVERED;
            default              -> false; // DELIVERED and CANCELLED are terminal
        };
    }

    @Override
    public String toString() {
        return "Order{id=" + orderId + ", customerId=" + customerId
               + ", status=" + orderStatus + ", finalAmount=" + finalAmount + '}';
    }
}
