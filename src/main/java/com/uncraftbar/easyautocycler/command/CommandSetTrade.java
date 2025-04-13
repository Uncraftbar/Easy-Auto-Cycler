package com.uncraftbar.easyautocycler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.uncraftbar.easyautocycler.AutomationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class CommandSetTrade {

    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ENCHANTMENT = new DynamicCommandExceptionType(
            id -> Component.translatable("commands.easyautocycler.setbook.unknown_enchantment", id)
    );
    private static final DynamicCommandExceptionType ERROR_INVALID_LEVEL = new DynamicCommandExceptionType(
            levelInfo -> Component.translatable("commands.easyautocycler.setbook.invalid_level", levelInfo)
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("autocycle")
                .then(Commands.literal("setbook")
                        .then(Commands.argument("enchantment", ResourceLocationArgument.id())
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("max_emeralds", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> setTrade(ctx.getSource(),
                                                        ResourceLocationArgument.getId(ctx, "enchantment"),
                                                        IntegerArgumentType.getInteger(ctx, "level"),
                                                        IntegerArgumentType.getInteger(ctx, "max_emeralds")
                                                ))
                                        )
                                )
                        )
                )
                .then(Commands.literal("clear")
                        .executes(ctx -> clearTrade(ctx.getSource()))
                )
                .then(Commands.literal("status")
                        .executes(ctx -> showStatus(ctx.getSource()))
                )
        );
    }

    private static int setTrade(CommandSourceStack source, ResourceLocation enchantmentId, int level, int maxEmeralds) throws CommandSyntaxException {
        Registry<Enchantment> enchantmentRegistry = source.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Enchantment enchantment = enchantmentRegistry.getOptional(enchantmentId)
                .orElseThrow(() -> ERROR_UNKNOWN_ENCHANTMENT.create(enchantmentId));

        if (level < 1 || level > enchantment.getMaxLevel()) {
            throw ERROR_INVALID_LEVEL.create(level + " (Min: 1, Max for " + enchantmentId.getPath() + ": " + enchantment.getMaxLevel() + ")");
        }

        AutomationManager.INSTANCE.configureTarget(enchantment, enchantmentId, level, maxEmeralds);

        return 1;
    }

    private static int clearTrade(CommandSourceStack source) {
        AutomationManager.INSTANCE.clearTarget();
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ResourceLocation currentTargetId = AutomationManager.INSTANCE.getTargetEnchantmentId();
        Component message;
        if (currentTargetId != null) {
            message = Component.translatable("commands.easyautocycler.status.set.simple", // Changed key
                    currentTargetId.toString(),
                    AutomationManager.INSTANCE.getTargetLevel(),
                    AutomationManager.INSTANCE.getMaxEmeraldCost()
            );
        } else {
            message = Component.translatable("commands.easyautocycler.status.none");
        }
        source.sendSuccess(() -> message, false);
        return 1;
    }
}