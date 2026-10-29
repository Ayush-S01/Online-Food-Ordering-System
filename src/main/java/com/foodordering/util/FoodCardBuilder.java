package com.foodordering.util;

import com.foodordering.model.CartItem;
import com.foodordering.model.FoodItem;
import com.foodordering.service.CartService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Reusable builder for Zomato-inspired food cards and compact sidebar cards.
 *
 * <p>Optimizations:
 * <ul>
 *   <li>Aspect-ratio preserving center-crop viewport calculation so images are never stretched.</li>
 *   <li>Reduced tile width (208px) for main cards so dishes look balanced, crisp, and centered.</li>
 *   <li>Generous width for sidebar cards so titles and buttons are fully visible without truncation.</li>
 *   <li>Prominently visible "+" and "-" steppers with zero clipping and standard ASCII symbols.</li>
 *   <li>Universal Unicode 6.0 cuisine glyphs preventing rectangle/tofu boxes across all Windows versions.</li>
 * </ul>
 */
public final class FoodCardBuilder {

    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();
    private static final Map<Integer, List<Consumer<CartService>>> SLOT_REGISTRY = new HashMap<>();

    private FoodCardBuilder() {}

    public static void clearSlotRegistry() {
        SLOT_REGISTRY.clear();
    }

    public static void registerSlotUpdater(int foodId, Consumer<CartService> updater) {
        SLOT_REGISTRY.computeIfAbsent(foodId, k -> new ArrayList<>()).add(updater);
    }

    public static void notifySlotChanged(int foodId, CartService cartService) {
        List<Consumer<CartService>> updaters = SLOT_REGISTRY.get(foodId);
        if (updaters != null) {
            for (Consumer<CartService> u : new ArrayList<>(updaters)) {
                try {
                    u.accept(cartService);
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Builds a standard food card for the main cuisine menu grid with reduced width
     * and aspect-ratio-preserved center-cropped photography.
     */
    public static VBox buildCard(FoodItem item, CartService cartService, Runnable onItemAdded, Runnable onCartChanged) {
        VBox card = new VBox(6);
        card.getStyleClass().add("food-card");
        // Compact tile width: 208px (reduced from 240px to eliminate image stretching)
        card.setPrefWidth(208);
        card.setMinWidth(208);
        card.setMaxWidth(208);
        card.setPrefHeight(340);
        card.setPadding(new Insets(8));

        // 1. Food Image: 192x132 (natural 1.45:1 aspect ratio with center-crop)
        Node imageNode = createFoodImageNode(item, 192, 132);
        card.getChildren().add(imageNode);

        // 2. Badges Row: Diet Icon + Cuisine Badge + Rating Badge
        HBox badgeRow = new HBox(5);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        Node vegIcon = createDietaryBadge(item.isVeg());

        Label cuisineBadge = new Label(item.getCuisine());
        cuisineBadge.getStyleClass().add("cuisine-badge");
        cuisineBadge.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Label ratingLabel = new Label("⭐ " + String.format("%.1f", item.getRating()));
        ratingLabel.getStyleClass().add("rating-badge");
        ratingLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");

        badgeRow.getChildren().addAll(vegIcon, cuisineBadge, spacer1, ratingLabel);
        card.getChildren().add(badgeRow);

        // 3. Dish Name
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("food-name");
        nameLabel.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        nameLabel.setWrapText(true);
        nameLabel.setMinHeight(34);
        nameLabel.setMaxHeight(34);
        card.getChildren().add(nameLabel);

        // 4. Meta Sub-row: Prep time & Category
        HBox metaRow = new HBox(4);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label prepLabel = new Label("⏱️ " + item.getPrepTime());
        prepLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
        Label dotLabel = new Label("•");
        dotLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #BBB;");
        Label catLabel = new Label(item.getCategory());
        catLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
        metaRow.getChildren().addAll(prepLabel, dotLabel, catLabel);
        card.getChildren().add(metaRow);

        // 5. Short Description
        Label descLabel = new Label(item.getDescription() != null ? item.getDescription() : "");
        descLabel.getStyleClass().add("food-desc");
        descLabel.setStyle("-fx-font-size: 10.5px;");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(28);
        descLabel.setMaxHeight(28);
        card.getChildren().add(descLabel);

        Region spacer2 = new Region();
        VBox.setVgrow(spacer2, Priority.ALWAYS);
        card.getChildren().add(spacer2);

        // 6. Bottom Row: Price & Self-Updating Zomato Add/Stepper Slot
        HBox bottomRow = new HBox(6);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(item.getFormattedPrice());
        priceLabel.getStyleClass().add("food-price");
        priceLabel.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FF5E36;");
        HBox.setHgrow(priceLabel, Priority.ALWAYS);

        StackPane cartSlot = new StackPane();
        cartSlot.setAlignment(Pos.CENTER_RIGHT);
        cartSlot.setMinWidth(76);
        updateCartSlot(cartSlot, item, cartService, onItemAdded, onCartChanged);
        registerSlotUpdater(item.getFoodId(), cs -> updateCartSlot(cartSlot, item, cs, onItemAdded, onCartChanged));

        bottomRow.getChildren().addAll(priceLabel, cartSlot);
        card.getChildren().add(bottomRow);

        return card;
    }

    public static VBox buildCard(FoodItem item, CartService cartService, Runnable onCartChanged) {
        return buildCard(item, cartService, onCartChanged, onCartChanged);
    }

    /**
     * Builds a wide horizontal mini-card for Today's Special in the right sidebar.
     * With generous width so all title characters, ratings, prices, and buttons are fully visible.
     */
    public static HBox buildMiniCard(FoodItem item, CartService cartService, Runnable onItemAdded, Runnable onCartChanged) {
        HBox card = new HBox(10);
        card.getStyleClass().add("mini-food-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(405);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(10));

        // 1. Food Image Thumbnail: 75x75 with center-crop
        Node imageNode = createFoodImageNode(item, 75, 75);
        card.getChildren().add(imageNode);

        // 2. Middle Details: Name, Diet, Rating, Price
        VBox details = new VBox(4);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, Priority.ALWAYS);

        HBox titleRow = new HBox(6);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Node vegIcon = createDietaryBadge(item.isVeg());
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(205); // Expanded width so long names fit easily
        titleRow.getChildren().addAll(vegIcon, nameLabel);

        Label metaLabel = new Label("⭐ " + String.format("%.1f", item.getRating()) + " • " + item.getCuisine());
        metaLabel.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #7F8C8D;");

        Label priceLabel = new Label(item.getFormattedPrice());
        priceLabel.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FF5E36;");

        details.getChildren().addAll(titleRow, metaLabel, priceLabel);
        card.getChildren().add(details);

        // 3. In-place Add / Stepper Slot
        StackPane cartSlot = new StackPane();
        cartSlot.setAlignment(Pos.CENTER_RIGHT);
        cartSlot.setMinWidth(85);
        cartSlot.setPrefWidth(88);
        updateCartSlot(cartSlot, item, cartService, onItemAdded, onCartChanged);
        registerSlotUpdater(item.getFoodId(), cs -> updateCartSlot(cartSlot, item, cs, onItemAdded, onCartChanged));

        card.getChildren().add(cartSlot);
        return card;
    }

    public static HBox buildMiniCard(FoodItem item, CartService cartService, Runnable onCartChanged) {
        return buildMiniCard(item, cartService, onCartChanged, onCartChanged);
    }

    /**
     * Builds a compact add-on recommendation card specifically designed for inside the Cart Drawer.
     * Visually distinctive from ordered cart items so they do not blend together.
     */
    public static HBox buildCartRecommendationCard(FoodItem item, CartService cartService, Runnable onItemAdded, Runnable onCartChanged) {
        HBox card = new HBox(8);
        card.getStyleClass().add("cart-rec-item-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(6, 10, 6, 8));

        // 1. Food Image Thumbnail: 56x56
        Node imageNode = createFoodImageNode(item, 56, 56);
        card.getChildren().add(imageNode);

        // 2. Middle Details
        VBox details = new VBox(2);
        details.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(details, Priority.ALWAYS);

        HBox titleRow = new HBox(5);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Node vegIcon = createDietaryBadge(item.isVeg());
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);
        titleRow.getChildren().addAll(vegIcon, nameLabel);

        Label metaLabel = new Label("⭐ " + String.format("%.1f", item.getRating()) + " • " + item.getCuisine());
        metaLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7F8C8D;");

        Label priceLabel = new Label(item.getFormattedPrice());
        priceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #FF5E36;");

        details.getChildren().addAll(titleRow, metaLabel, priceLabel);
        card.getChildren().add(details);

        // 3. Add Slot
        StackPane cartSlot = new StackPane();
        cartSlot.setAlignment(Pos.CENTER_RIGHT);
        cartSlot.setMinWidth(80);
        updateCartSlot(cartSlot, item, cartService, onItemAdded, onCartChanged);
        registerSlotUpdater(item.getFoodId(), cs -> updateCartSlot(cartSlot, item, cs, onItemAdded, onCartChanged));

        card.getChildren().add(cartSlot);
        return card;
    }

    /**
     * Updates only this card's specific cart button / stepper in-place.
     * Fires onItemAdded when newly added, and onCartChanged on modification.
     */
    private static void updateCartSlot(StackPane slot, FoodItem item, CartService cartService,
                                       Runnable onItemAdded, Runnable onCartChanged) {
        slot.getChildren().clear();

        if (!item.isAvailable()) {
            Button soldOutBtn = new Button("Sold Out");
            soldOutBtn.getStyleClass().add("unavailable-btn");
            soldOutBtn.setDisable(true);
            slot.getChildren().add(soldOutBtn);
            return;
        }

        CartItem inCart = cartService.getCart().getItem(item.getFoodId());
        if (inCart != null && inCart.getQuantity() > 0) {
            // Zomato Stepper: [-] [qty] [+] with guaranteed visible white symbols
            HBox stepper = new HBox(4);
            stepper.setAlignment(Pos.CENTER);
            stepper.setStyle("-fx-background-color: #E8F8F5; -fx-border-color: #27AE60; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 2 4 2 4;");

            Button minusBtn = new Button("-");
            minusBtn.getStyleClass().add("qty-btn-small");
            minusBtn.setMinWidth(24);
            minusBtn.setPrefWidth(24);
            minusBtn.setMaxWidth(24);
            minusBtn.setMinHeight(24);
            minusBtn.setPrefHeight(24);
            minusBtn.setMaxHeight(24);
            minusBtn.setPadding(Insets.EMPTY);
            minusBtn.setAlignment(Pos.CENTER);
            minusBtn.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0;");
            minusBtn.setOnAction(e -> {
                cartService.decrementQuantity(item.getFoodId());
                notifySlotChanged(item.getFoodId(), cartService);
                if (onCartChanged != null) onCartChanged.run();
            });

            Label qtyLabel = new Label(String.valueOf(inCart.getQuantity()));
            qtyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #27AE60; -fx-padding: 0 4 0 4;");
            qtyLabel.setMinWidth(18);
            qtyLabel.setAlignment(Pos.CENTER);

            Button plusBtn = new Button("+");
            plusBtn.getStyleClass().add("qty-btn-small");
            plusBtn.setMinWidth(24);
            plusBtn.setPrefWidth(24);
            plusBtn.setMaxWidth(24);
            plusBtn.setMinHeight(24);
            plusBtn.setPrefHeight(24);
            plusBtn.setMaxHeight(24);
            plusBtn.setPadding(Insets.EMPTY);
            plusBtn.setAlignment(Pos.CENTER);
            plusBtn.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 0;");
            plusBtn.setOnAction(e -> {
                cartService.incrementQuantity(item.getFoodId());
                notifySlotChanged(item.getFoodId(), cartService);
                if (onCartChanged != null) onCartChanged.run();
            });

            stepper.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
            slot.getChildren().add(stepper);
        } else {
            // Zomato "+ ADD" button
            Button addBtn = new Button("+ ADD");
            addBtn.getStyleClass().add("add-btn");
            addBtn.setMinWidth(75);
            addBtn.setPrefWidth(76);
            addBtn.setOnAction(e -> {
                cartService.addToCart(item);
                notifySlotChanged(item.getFoodId(), cartService);
                if (onItemAdded != null) {
                    onItemAdded.run();
                } else if (onCartChanged != null) {
                    onCartChanged.run();
                }
            });
            slot.getChildren().add(addBtn);
        }
    }

    /**
     * Loads dish image from classpath with in-memory caching and viewport center-crop
     * to eliminate any image stretching or aspect-ratio distortion.
     * Guaranteed to look up explicit imagePath first, then automatically resolve by dish name.
     */
    private static Node createFoodImageNode(FoodItem item, double width, double height) {
        Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(12);
        clip.setArcHeight(12);

        Image cachedImage = resolveFoodImage(item);
        if (cachedImage != null) {
            ImageView iv = new ImageView(cachedImage);
            double imgW = cachedImage.getWidth();
            double imgH = cachedImage.getHeight();
            if (imgW > 0 && imgH > 0) {
                double targetAspect = width / height;
                double imgAspect = imgW / imgH;
                double cropX = 0, cropY = 0, cropW = imgW, cropH = imgH;
                if (imgAspect > targetAspect) {
                    cropW = imgH * targetAspect;
                    cropX = (imgW - cropW) / 2.0;
                } else {
                    cropH = imgW / targetAspect;
                    cropY = (imgH - cropH) / 2.0;
                }
                iv.setViewport(new Rectangle2D(cropX, cropY, cropW, cropH));
            }
            iv.setFitWidth(width);
            iv.setFitHeight(height);
            iv.setSmooth(true);
            iv.setClip(clip);
            return iv;
        }

        // Fallback: Gradient banner with culinary emoji
        StackPane placeholder = new StackPane();
        placeholder.setPrefSize(width, height);
        placeholder.setMinSize(width, height);
        placeholder.setMaxSize(width, height);
        placeholder.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFEAA7, #FAB1A0); -fx-background-radius: 8;");
        placeholder.setClip(clip);

        Label emojiLabel = new Label(getCuisineEmoji(item.getCuisine()));
        emojiLabel.setStyle("-fx-font-size: " + (height > 80 ? "36px" : "24px") + ";");
        placeholder.getChildren().add(emojiLabel);
        return placeholder;
    }

    /**
     * Resolves the Image for a given FoodItem.
     * 1. Checks explicit imagePath (as /images/<path>, /<path>, or <path>).
     * 2. Automatically falls back to sanitized dish name slug (/images/<slug>.png).
     */
    public static Image resolveFoodImage(FoodItem item) {
        if (item == null) return null;

        // 1. Try explicit imagePath if configured
        String rawPath = item.getImagePath();
        if (rawPath != null && !rawPath.isBlank()) {
            String trimmed = rawPath.trim();
            // Try with /images/ prefix
            String subPath = trimmed.replaceFirst("^/+", "").replaceFirst("^images/+", "");
            Image img = tryLoadImage("/images/" + subPath);
            if (img != null) return img;

            // Try raw path
            img = tryLoadImage(trimmed);
            if (img != null) return img;
        }

        // 2. Try automatic resolution by dish name slug
        String name = item.getName();
        if (name != null && !name.isBlank()) {
            String slug = toImageSlug(name);
            Image img = tryLoadImage("/images/" + slug + ".png");
            if (img != null) return img;
            img = tryLoadImage("/images/" + slug + ".jpg");
            if (img != null) return img;
        }

        return null;
    }

    /**
     * Converts a dish name to its standardized image filename slug.
     * E.g. "Mango Lassi" -> "mango_lassi", "Gulab Jamun (2 pcs)" -> "gulab_jamun_2_pcs"
     */
    public static String toImageSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
            .replace("\"", "")
            .replace("'", "")
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }

    private static Image tryLoadImage(String path) {
        if (path == null || path.isBlank()) return null;
        if (!path.startsWith("/")) path = "/" + path;
        Image cached = IMAGE_CACHE.get(path);
        if (cached != null) return cached;

        try {
            URL url = FoodCardBuilder.class.getResource(path);
            if (url != null) {
                Image img = new Image(url.toExternalForm(), false);
                if (!img.isError() && img.getWidth() > 0) {
                    IMAGE_CACHE.put(path, img);
                    return img;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static Node createDietaryBadge(boolean isVeg) {
        StackPane box = new StackPane();
        box.setPrefSize(14, 14);
        box.setMinSize(14, 14);
        box.setMaxSize(14, 14);
        String color = isVeg ? "#27AE60" : "#E74C3C";
        box.setStyle("-fx-border-color: " + color + "; -fx-border-width: 1.5; -fx-border-radius: 3; -fx-background-color: white; -fx-background-radius: 3;");
        Circle dot = new Circle(3.5, Paint.valueOf(color));
        box.getChildren().add(dot);
        Tooltip.install(box, new Tooltip(isVeg ? "Pure Vegetarian" : "Non-Vegetarian"));
        return box;
    }

    /**
     * Standard Unicode 6.0 cuisine emojis supported natively by Windows JavaFX.
     * Guaranteed to never render as hollow rectangle / tofu boxes.
     */
    public static String getCuisineEmoji(String cuisine) {
        if (cuisine == null) return "🍲";
        return switch (cuisine.trim()) {
            case "North Indian" -> "🍛";
            case "South Indian" -> "🥞";
            case "Chinese"      -> "🍜"; // Universally supported steaming bowl (no tofu box!)
            case "Italian"      -> "🍕";
            case "Mexican"      -> "🌮";
            case "Fast Food"    -> "🍔";
            case "Biryani & Mughlai", "Biryani" -> "🍚";
            case "Desserts"     -> "🍰";
            case "Beverages"    -> "☕";
            default             -> "🍲";
        };
    }
}
