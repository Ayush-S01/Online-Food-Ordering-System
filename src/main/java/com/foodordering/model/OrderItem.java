package com.foodordering.model;

import java.math.BigDecimal;

/**
 * Represents one line within a placed order.
 *
 * <p>Food name and unit price are snapshots captured at order-placement time
 * so that historical orders remain accurate even after the menu changes.
 *
 * <p>OOP: Encapsulation.
 */
public class OrderItem {

    private int orderItemId;
    private int orderId;
    private int foodId;
    private String foodNameSnapshot;   // historical name
    private BigDecimal unitPrice;      // historical price
    private int quantity;
    private BigDecimal lineTotal;      // unitPrice × quantity

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public OrderItem() {}

    public OrderItem(int orderItemId, int orderId, int foodId,
                     String foodNameSnapshot, BigDecimal unitPrice,
                     int quantity, BigDecimal lineTotal) {
        this.orderItemId      = orderItemId;
        this.orderId          = orderId;
        this.foodId           = foodId;
        this.foodNameSnapshot = foodNameSnapshot;
        this.unitPrice        = unitPrice;
        this.quantity         = quantity;
        this.lineTotal        = lineTotal;
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public int getOrderItemId()                        { return orderItemId;           }
    public void setOrderItemId(int id)                 { this.orderItemId = id;        }

    public int getOrderId()                            { return orderId;               }
    public void setOrderId(int id)                     { this.orderId = id;            }

    public int getFoodId()                             { return foodId;                }
    public void setFoodId(int id)                      { this.foodId = id;             }

    public String getFoodNameSnapshot()                { return foodNameSnapshot;      }
    public void setFoodNameSnapshot(String name)       { this.foodNameSnapshot = name; }

    public BigDecimal getUnitPrice()                   { return unitPrice;             }
    public void setUnitPrice(BigDecimal price)         { this.unitPrice = price;       }

    public int getQuantity()                           { return quantity;              }
    public void setQuantity(int qty)                   { this.quantity = qty;          }

    public BigDecimal getLineTotal()                   { return lineTotal;             }
    public void setLineTotal(BigDecimal total)         { this.lineTotal = total;       }

    @Override
    public String toString() {
        return "OrderItem{orderId=" + orderId + ", food='" + foodNameSnapshot
               + "', qty=" + quantity + ", lineTotal=" + lineTotal + '}';
    }
}
