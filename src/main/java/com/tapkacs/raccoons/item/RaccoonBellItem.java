package com.tapkacs.raccoons.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import com.tapkacs.raccoons.advancement.ModTriggers;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Bell that calls all of the player's nearby tamed raccoons over when rung (right-click into the air).
 * Sways in hand while walking; both are purely cosmetic GeckoLib animations on the held-item model.
 */
public class RaccoonBellItem extends Item implements GeoItem {
    private static final double CALL_RADIUS = 32.0;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RaccoonBellItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return this.ring(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = this.ring(context.getLevel(), context.getPlayer(), context.getHand());
        return result != InteractionResult.PASS ? result : super.useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        // Also rings when the crosshair happens to land directly on an entity (e.g. one of your own raccoons) -
        // vanilla routes that click through here instead of use()/useOn(), and never falls back to those on its own.
        return this.ring(player.level(), player, hand);
    }

    /** Right-clicking with the bell rings it regardless of whether the crosshair happens to be on a block - it doesn't need truly empty air. */
    private InteractionResult ring(Level level, @Nullable Player player, InteractionHand hand) {
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            this.callNearbyRaccoons(serverLevel, player);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 2.0F, 1.0F);
        }
        if (player instanceof ServerPlayer) {
            this.triggerAnim(player, GeoItem.getId(player.getItemInHand(hand)), "bell", "ring");
        }
        return InteractionResult.SUCCESS;
    }

    private void callNearbyRaccoons(ServerLevel level, Player player) {
        AABB area = player.getBoundingBox().inflate(CALL_RADIUS);
        List<RaccoonEntity> raccoons = level.getEntitiesOfClass(RaccoonEntity.class, area,
                raccoon -> raccoon.isTame() && raccoon.isOwnedBy(player));

        for (RaccoonEntity raccoon : raccoons) {
            raccoon.setBehaviorMode(RaccoonEntity.BehaviorMode.FOLLOW);
        }

        if (!raccoons.isEmpty() && player instanceof ServerPlayer serverPlayer) {
            ModTriggers.CALLED_RACCOON.trigger(serverPlayer);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                 Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable("item.raccoons.raccoon_bell.tooltip.flavor").withStyle(ChatFormatting.GOLD));
        tooltipAdder.accept(Component.translatable("item.raccoons.raccoon_bell.tooltip.hint").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RaccoonBellItem>("bell", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walking"));
            }
            return PlayState.STOP;
        }).triggerableAnim("ring", RawAnimation.begin().thenPlay("ring")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<RaccoonBellItem> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new RaccoonBellItemGeoModel()).withScale(1.1f);
                }
                return this.renderer;
            }
        });
    }
}
