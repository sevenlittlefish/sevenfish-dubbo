package com.sevenfish.common;

public class InventoryData {

    private static volatile int inventory = 1000;

    public static int getInventory() {
        return inventory;
    }

    public static void setInventory(int inventory) {
        InventoryData.inventory = inventory;
    }
}
