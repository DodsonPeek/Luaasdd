package com.sl1wed.addon.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import com.sl1wed.addon.Categories;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class AutoResell extends Module {
    private enum State {
        OPENING_SHOP,
        WAITING_FOR_GUI,
        NAVIGATING_TO_GEAR,
        NAVIGATING_TO_PEARL,
        BUYING_PEARLS,
        CLOSING_GUI,
        SELLING_PEARLS
    }
    
    private State state = State.OPENING_SHOP;
    private int delayTicks = 0;
    private int purchasedThisCycle = 0;
    
    private final Setting<Integer> buyDelay = settings.add(new IntSetting.Builder()
        .name("buy-delay")
        .description("Delay between purchases in ticks (0 = instant)")
        .defaultValue(2)
        .min(0)
        .max(20)
        .build()
    );
    
    private final Setting<Integer> invFillAmount = settings.add(new IntSetting.Builder()
        .name("inv-fill-amount")
        .description("How many slots to fill before selling")
        .defaultValue(32)
        .min(10)
        .max(36)
        .build()
    );
    
    public AutoResell() {
        super(Categories.SL1WED, "auto-resell", "Automatically buys and sells pearls");
    }
    
    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        switch (state) {
            case OPENING_SHOP:
                if (mc.currentScreen == null) {
                    ChatUtils.sendPlayerMsg("/shop");
                    state = State.WAITING_FOR_GUI;
                    delayTicks = 0;
                }
                break;
                
            case WAITING_FOR_GUI:
                delayTicks++;
                if (mc.currentScreen instanceof GenericContainerScreen) {
                    state = State.NAVIGATING_TO_GEAR;
                    delayTicks = 0;
                } else if (delayTicks > 40) { // Timeout after 2 seconds
                    state = State.OPENING_SHOP;
                }
                break;
                
            case NAVIGATING_TO_GEAR:
                if (mc.currentScreen instanceof GenericContainerScreen) {
                    int gearSlot = findItemSlot(Items.END_CRYSTAL);
                    if (gearSlot != -1) {
                        clickSlot(gearSlot);
                        state = State.NAVIGATING_TO_PEARL;
                    }
                }
                break;
                
            case NAVIGATING_TO_PEARL:
                if (mc.currentScreen instanceof GenericContainerScreen) {
                    int pearlSlot = findItemSlot(Items.ENDER_PEARL);
                    if (pearlSlot != -1) {
                        clickSlot(pearlSlot);
                        state = State.BUYING_PEARLS;
                    }
                }
                break;
                
            case BUYING_PEARLS:
                delayTicks++;
                if (delayTicks >= buyDelay.get()) {
                    if (mc.currentScreen instanceof GenericContainerScreen) {
                        int glassSlot = findGreenGlassSlot("16");
                        if (glassSlot != -1) {
                            clickSlot(glassSlot);
                            purchasedThisCycle++;
                            delayTicks = 0;
                            
                            if (getEmptySlots() <= (36 - invFillAmount.get())) {
                                state = State.CLOSING_GUI;
                            }
                        }
                    }
                }
                break;
                
            case CLOSING_GUI:
                if (mc.currentScreen != null) {
                    mc.currentScreen.close();
                }
                state = State.SELLING_PEARLS;
                delayTicks = 0;
                break;
                
            case SELLING_PEARLS:
                delayTicks++;
                if (delayTicks >= 10 && mc.currentScreen == null) {
                    ChatUtils.sendPlayerMsg("/sell");
                    purchasedThisCycle = 0;
                    state = State.OPENING_SHOP;
                    delayTicks = 0;
                }
                break;
        }
    }
    
    private int findItemSlot(net.minecraft.item.Item item) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return -1;
        
        for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
            ItemStack stack = screen.getScreenHandler().getSlot(i).getStack();
            if (stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }
    
    private int findGreenGlassSlot(String name) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return -1;
        
        for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
            ItemStack stack = screen.getScreenHandler().getSlot(i).getStack();
            if (stack.getItem() == Items.GREEN_STAINED_GLASS_PANE) {
                Text customName = stack.getName();
                if (customName.getString().contains(name)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private void clickSlot(int slot) {
        if (mc.interactionManager != null && mc.player != null) {
            mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slot,
                0,
                SlotActionType.PICKUP,
                mc.player
            );
        }
    }
    
    private int getEmptySlots() {
        int empty = 0;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) empty++;
        }
        return empty;
    }
}
