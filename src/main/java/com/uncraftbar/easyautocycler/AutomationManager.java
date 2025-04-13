package com.uncraftbar.easyautocycler; // Use your actual package name

// Imports for Minecraft and other libraries (should be mostly the same)
import de.maxhenkel.easyvillagers.gui.CycleTradesButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.Screen; // Was missing before, needed for logging/checks
import net.minecraft.client.resources.sounds.SimpleSoundInstance; // For sound
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents; // For sound events
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper; // Import for NBT enchantment access
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

// Standard Java imports
import javax.annotation.Nullable;
import java.util.Map; // Import Map for enchantment iteration
import java.util.concurrent.atomic.AtomicBoolean;

// Removed imports specific to 1.21.1 Data Components:
// import net.minecraft.core.component.DataComponents;
// import net.minecraft.core.Holder;
// import net.minecraft.world.item.enchantment.ItemEnchantments;


public class AutomationManager {


    public static final AutomationManager INSTANCE = new AutomationManager();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private CycleTradesButton targetButton = null;
    private int delayTicks = 0;
    private int currentCycles = 0;
    private static final int MAX_CYCLES_SAFETY = 3000;
    // You might want to adjust this for 1.20.1 performance if needed
    private static final int CLICK_DELAY = 2; // Default speed adjustment

    // Configuration Fields (remain the same)
    @Nullable private Enchantment targetEnchantment = null;
    @Nullable private ResourceLocation targetEnchantmentId = null;
    private int maxEmeraldCost = 64;
    private int targetLevel = 1;

    private AutomationManager() {}

    // Getters (remain the same)
    public boolean isRunning() { return isRunning.get(); }
    @Nullable public Enchantment getTargetEnchantment() { return targetEnchantment; }
    @Nullable public ResourceLocation getTargetEnchantmentId() { return targetEnchantmentId; }
    public int getMaxEmeraldCost() { return maxEmeraldCost; }
    public int getTargetLevel() { return targetLevel; }
    @Nullable public CycleTradesButton getTargetButton() { return this.targetButton; }


    // Internal State Management (remain the same)
    public void setTargetButton(CycleTradesButton button) {
        this.targetButton = button;
        EasyAutoCyclerMod.LOGGER.trace("Target button set: {}", button != null);
    }

    public void clearTargetButton() {
        this.targetButton = null;
        if (isRunning.get()) {
            stop("Button lost during cycle");
        }
    }

    // Public Controls (remain the same)
    // configureTarget signature already updated previously to remove book cost
    public void configureTarget(Enchantment enchantment, ResourceLocation enchantmentId, int level, int emeraldCost) {
        this.targetEnchantment = enchantment;
        this.targetEnchantmentId = enchantmentId;
        this.targetLevel = level;
        this.maxEmeraldCost = emeraldCost;

        String message = String.format("Set target: Enchantment=%s, Level=%d, MaxEmeralds=%d (Book cost: 1)",
                enchantmentId.toString(), level, emeraldCost);
        EasyAutoCyclerMod.LOGGER.info(message);
        sendMessageToPlayer(Component.literal(message));
    }

    public void clearTarget() {
        this.targetEnchantment = null;
        this.targetEnchantmentId = null;
        EasyAutoCyclerMod.LOGGER.info("Cleared target trade.");
        sendMessageToPlayer(Component.literal("Cleared target trade. Automation will not stop automatically."));
    }

    public void toggle() {
        if (isRunning.get()) {
            stop("Toggled off by user");
        } else {
            start();
        }
    }

    // Start method (remains the same, logging included)
    private void start() {
        Screen currentScreen = Minecraft.getInstance().screen;
        String screenName = (currentScreen != null) ? currentScreen.getClass().getName() : "null";
        EasyAutoCyclerMod.LOGGER.debug("AutomationManager.start(): Checking screen. Current screen is: {}", screenName);

        if (!(currentScreen instanceof MerchantScreen)) {
            sendMessageToPlayer(Component.literal("Error: Villager trade screen not open."));
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Screen check failed. Screen was: {}", screenName);
            return;
        }

        if (targetButton == null) {
            sendMessageToPlayer(Component.literal("Error: Cycle button not found (Easy Villagers issue?)."));
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Button not found.");
            return;
        }


        if (targetEnchantment == null) {
            sendMessageToPlayer(Component.literal("Warning: No target trade configured. Cycling will not stop automatically."));
            EasyAutoCyclerMod.LOGGER.warn("Starting cycle without target definition.");
        }

        if (isRunning.compareAndSet(false, true)) {
            EasyAutoCyclerMod.LOGGER.info("Starting villager trade cycling.");
            sendMessageToPlayer(Component.literal("Auto-cycling started. Press keybind again to stop."));
            this.delayTicks = 0;
            this.currentCycles = 0;
        }
    }

    // Stop method (remains the same)
    public void stop(String reason) {
        if (isRunning.compareAndSet(true, false)) {
            EasyAutoCyclerMod.LOGGER.info("Stopping villager trade cycling. Reason: {}", reason);
            sendMessageToPlayer(Component.literal("Auto-cycling stopped: " + reason));
        }
    }

    // Core Loop Logic (clientTick) - Sound playing logic included
    public void clientTick() {
        if (!isRunning.get()) return;


        if (!(Minecraft.getInstance().screen instanceof MerchantScreen screen) || targetButton == null) {
            stop("Screen closed or button lost");
            return;
        }



        currentCycles++;
        if (currentCycles > MAX_CYCLES_SAFETY) {
            stop("Max cycles safety limit reached");
            return;
        }

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        MerchantOffers offers = screen.getMenu().getOffers();
        if (targetEnchantment != null && checkTrades(offers)) { // checkTrades logic below is updated
            EasyAutoCyclerMod.LOGGER.info("Target trade FOUND!");
            sendMessageToPlayer(Component.literal("§aTarget trade found!"));

            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getSoundManager() != null) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F));
                }
            } catch (Exception e) {
                EasyAutoCyclerMod.LOGGER.error("Failed to play 'trade found' sound effect", e);
            }

            stop("Target trade found");
            return;
        }


        if (!targetButton.visible || !targetButton.active) {
            EasyAutoCyclerMod.LOGGER.trace("Cycle button not active, waiting...");
            return;
        }

        EasyAutoCyclerMod.LOGGER.trace("Clicking Cycle Trades button (Cycle {})", currentCycles);
        targetButton.onPress();
        delayTicks = CLICK_DELAY;
    }



    // --- Trade Checking Logic (checkTrades) - UPDATED FOR 1.20.1 NBT ---
    // TODO: Refactor checkTrades and configuration for general item trades.
    private boolean checkTrades(MerchantOffers offers) {
        if (targetEnchantment == null) return false; // No target set

        for (MerchantOffer offer : offers) {
            ItemStack resultStack = offer.getResult();

            // Is the result an enchanted book item?
            if (!resultStack.is(Items.ENCHANTED_BOOK)) {
                continue; // Skip if not a book
            }

            // Is the offer currently available?
            if (offer.isOutOfStock()) {
                continue; // Skip disabled trades
            }

            // Check ingredients (1 Book + Emeralds <= max)
            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            final int requiredBookCost = 1; // Hardcoded book cost

            if (costA.is(Items.EMERALD) && costA.getCount() <= this.maxEmeraldCost) {
                // If A is emeralds within price, B must be exactly 1 book
                if (!costB.is(Items.BOOK) || costB.getCount() != requiredBookCost) {
                    continue; // Cost B is wrong
                }
            } else if (costB.is(Items.EMERALD) && costB.getCount() <= this.maxEmeraldCost) {
                // If B is emeralds within price, A must be exactly 1 book
                if (!costA.is(Items.BOOK) || costA.getCount() != requiredBookCost) {
                    continue; // Cost A is wrong
                }
            } else {
                // Neither ingredient combination matched (Emeralds + 1 Book)
                continue;
            }

            // --- NBT Based Enchantment Check ---
            // Use EnchantmentHelper to get enchantments from the ItemStack's NBT data
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(resultStack);

            // Skip if the map is empty (no enchantments found)
            if (enchantments.isEmpty()) {
                continue;
            }

            boolean foundMatchingEnchantment = false;
            // Iterate through the Map of Enchantment -> Level
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment ench = entry.getKey(); // The enchantment object
                int level = entry.getValue();     // The enchantment level

                // Compare the enchantment object and level directly
                if (ench.equals(this.targetEnchantment) && level == this.targetLevel) {
                    foundMatchingEnchantment = true;
                    break; // Found the target enchantment, no need to check others on this book
                }
            }
            // --- End NBT Check ---

            // If we found the matching enchantment in the loop above, this offer is the one!
            if (foundMatchingEnchantment) {
                return true;
            }
        }

        return false; // No matching trade found after checking all offers
    }



    // --- Utility (sendMessageToPlayer) - Remains the same ---
    private void sendMessageToPlayer(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.player.sendSystemMessage(message);
        }
    }


}