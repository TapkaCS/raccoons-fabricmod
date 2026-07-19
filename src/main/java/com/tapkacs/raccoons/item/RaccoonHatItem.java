package com.tapkacs.raccoons.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;
import com.geckolib.util.GeckoLibUtil;
import com.tapkacs.raccoons.Raccoons;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** Straw hat item that renders its own 3D model (instead of the flat inventory icon) while held in hand. */
public class RaccoonHatItem extends Item implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RaccoonHatItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model, no animation needed - renders in its bind pose.
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                 Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable("item.raccoons.raccoon_hat.tooltip.desc").withStyle(ChatFormatting.GREEN));
        tooltipAdder.accept(Component.translatable("item.raccoons.raccoon_hat.tooltip.warning_prefix").withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("item.raccoons.raccoon_hat.tooltip.warning").withStyle(ChatFormatting.RED)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<RaccoonHatItem> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new RaccoonHatItemGeoModel())
                            .withScale(0.4f)
                            .withRenderLayer(r -> new CustomBoneTextureGeoLayer<RaccoonHatItem, GeoItemRenderer.RenderData, GeoRenderState>(r, "prouzek",
                                    Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "textures/entity/collar/red.png")));
                }
                return this.renderer;
            }
        });
    }
}
