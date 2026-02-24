package com.modoria.domain.product.enums;

/**
 * Represents the status of a product in the inventory.
 */
public enum ProductStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    OUT_OF_STOCK("Out of Stock");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

