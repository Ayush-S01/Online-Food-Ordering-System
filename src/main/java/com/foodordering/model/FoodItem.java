package com.foodordering.model;

import java.math.BigDecimal;

/**
 * Represents a food item in the menu.
 *
 * <p>Supports multi-cuisine attributes, dietary classification (Veg / Non-Veg),
 * customer ratings, and preparation time.
 *
 * <p>OOP: Encapsulation — all fields private; accessed through getters/setters.
 */
public class FoodItem {

    private int foodId;
    private String name;
    private String category;
    private String cuisine;
    private String description;
    private BigDecimal price;
    private String imagePath;
    private boolean isVeg;
    private double rating;
    private String prepTime;
    private boolean available;

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public FoodItem() {
        this.cuisine = "North Indian";
        this.isVeg = true;
        this.rating = 4.5;
        this.prepTime = "20-25 mins";
    }

    /**
     * Backward-compatible 7-argument constructor (used in existing tests & legacy callers).
     */
    public FoodItem(int foodId, String name, String category, String description,
                    BigDecimal price, String imagePath, boolean available) {
        this(foodId, name, category, "North Indian", description, price, imagePath, true, 4.5, "20-25 mins", available);
    }

    /**
     * Full multi-cuisine constructor.
     */
    public FoodItem(int foodId, String name, String category, String cuisine,
                    String description, BigDecimal price, String imagePath,
                    boolean isVeg, double rating, String prepTime, boolean available) {
        this.foodId      = foodId;
        this.name        = name;
        this.category    = category;
        this.cuisine     = (cuisine != null && !cuisine.isBlank()) ? cuisine : "North Indian";
        this.description = description;
        this.price       = price;
        this.imagePath   = imagePath;
        this.isVeg       = isVeg;
        this.rating      = rating;
        this.prepTime    = (prepTime != null && !prepTime.isBlank()) ? prepTime : "20-25 mins";
        this.available   = available;
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public int getFoodId()                     { return foodId;             }
    public void setFoodId(int foodId)          { this.foodId = foodId;      }

    public String getName()                    { return name;               }
    public void setName(String name)           { this.name = name;          }

    public String getCategory()                { return category;           }
    public void setCategory(String category)   { this.category = category;  }

    public String getCuisine()                 { return cuisine;            }
    public void setCuisine(String cuisine)     { this.cuisine = cuisine;    }

    public String getDescription()             { return description;        }
    public void setDescription(String desc)    { this.description = desc;   }

    public BigDecimal getPrice()                { return price;              }
    public void setPrice(BigDecimal price)      { this.price = price;        }

    public String getImagePath()                { return imagePath;          }
    public void setImagePath(String imagePath)  { this.imagePath = imagePath;}

    public boolean isVeg()                     { return isVeg;              }
    public void setVeg(boolean isVeg)          { this.isVeg = isVeg;        }

    public double getRating()                  { return rating;             }
    public void setRating(double rating)       { this.rating = rating;      }

    public String getPrepTime()                { return prepTime;           }
    public void setPrepTime(String prepTime)   { this.prepTime = prepTime;  }

    public boolean isAvailable()               { return available;          }
    public void setAvailable(boolean available){ this.available = available;}

    // ------------------------------------------------------------------ //
    //  Utility
    // ------------------------------------------------------------------ //

    /** Formatted price string for display (e.g. "₹ 180.00"). */
    public String getFormattedPrice() {
        return "₹ " + price.toPlainString();
    }

    @Override
    public String toString() {
        return "FoodItem{id=" + foodId + ", name='" + name + "', cuisine='" + cuisine + "', price=" + price + '}';
    }
}
