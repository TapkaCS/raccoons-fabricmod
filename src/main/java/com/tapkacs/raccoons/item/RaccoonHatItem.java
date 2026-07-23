package com.tapkacs.raccoons.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** Straw hat item - plain flat/geometric model, no custom rendering. */
public class RaccoonHatItem extends Item {

    public RaccoonHatItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                 Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable("item.raccoons.raccoon_hat.tooltip.desc").withStyle(ChatFormatting.GREEN));
        tooltipAdder.accept(Component.translatable("item.raccoons.raccoon_hat.tooltip.warning_prefix").withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("item.raccoons.raccoon_hat.tooltip.warning").withStyle(ChatFormatting.RED)));
    }
}
