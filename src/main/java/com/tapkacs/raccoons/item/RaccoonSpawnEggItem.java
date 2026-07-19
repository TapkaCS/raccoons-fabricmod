package com.tapkacs.raccoons.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import com.tapkacs.raccoons.Raccoons;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Consumer;

/** Spawn egg that renders the actual raccoon model (frozen on its idle pose) instead of the flat egg icon while held in hand. */
public class RaccoonSpawnEggItem extends SpawnEggItem implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RaccoonSpawnEggItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Frozen on the Idle pose's first frame - speed 0 means the animation timeline never advances, so it never moves.
        controllers.add(new AnimationController<RaccoonSpawnEggItem>("pose", 0, state -> {
            state.setControllerSpeed(0);
            return state.setAndContinue(RawAnimation.begin().thenLoop("Idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<RaccoonSpawnEggItem> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new DefaultedEntityGeoModel<RaccoonSpawnEggItem>(Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "raccoon")))
                            .withScale(0.4f);
                }
                return this.renderer;
            }
        });
    }
}
