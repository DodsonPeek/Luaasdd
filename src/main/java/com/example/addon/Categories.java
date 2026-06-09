package com.sl1wed.addon;

import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.MeteorClient;
import java.awt.Color;

public class Categories {
    public static final Category SL1WED = new Category(
        "Sl1wed's addon",
        MeteorClient.INSTANCE,
        new Color(255, 100, 100)  // Red color for the tab
    );
}
