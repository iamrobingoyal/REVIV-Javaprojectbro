// src/com/reviv/model/Resource.java
package com.reviv.model;

public class Resource {
    private int resourceId;
    private String resourceName;
    private int quantity;
    private int usedQuantity;

    // Empty constructor for database loading.
    public Resource() {
    }

    // Full constructor with all fields.
    public Resource(int resourceId, String resourceName, int quantity, int usedQuantity) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.quantity = quantity;
        this.usedQuantity = usedQuantity;
    }

    // Returns the resourceId of this resource.
    public int getResourceId() {
        return resourceId;
    }

    // Sets the resourceId of this resource.
    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    // Returns the resourceName of this resource.
    public String getResourceName() {
        return resourceName;
    }

    // Sets the resourceName of this resource.
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    // Returns the quantity of this resource.
    public int getQuantity() {
        return quantity;
    }

    // Sets the quantity of this resource.
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Returns the usedQuantity of this resource.
    public int getUsedQuantity() {
        return usedQuantity;
    }

    // Sets the usedQuantity of this resource.
    public void setUsedQuantity(int usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    // Returns a text view of this resource.
    @Override
    public String toString() {
        return "Resource [resourceId=" + resourceId + ", resourceName=" + resourceName + ", quantity=" + quantity + ", usedQuantity=" + usedQuantity + "]";
    }
}
