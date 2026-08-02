package com.tapkacs.raccoons.client.entity;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jspecify.annotations.Nullable;

public class RaccoonRenderer extends GeoEntityRenderer<RaccoonEntity, RaccoonRenderState> {
    public RaccoonRenderer(EntityRendererProvider.Context context) {
        super(context, new RaccoonGeoModel());
        this.withRenderLayer(new RaccoonMouthItemGeoLayer(context, this));
        this.withRenderLayer(new RaccoonHatGeoLayer(this));
        this.withRenderLayer(new RaccoonHatBandGeoLayer(this));
    }

    @Override
    public RaccoonRenderState createRenderState(RaccoonEntity animatable, @Nullable Void relatedObject) {
        return new RaccoonRenderState();
    }

    // The baby geo is already modeled at true baby size, but GeckoLib additionally multiplies by
    // vanilla's age shrink (renderState.scale = 0.5 for babies), which would quarter-size it.
    // Cancel that factor for babies; the entity's hitbox keeps vanilla's baby scaling.
    @Override
    public void scaleModelForRender(RenderPassInfo<RaccoonRenderState> info, float widthScale, float heightScale) {
        RaccoonRenderState state = info.renderState();
        if (state.isBaby && state.scale > 0) {
            super.scaleModelForRender(info, widthScale / state.scale, heightScale / state.scale);
            return;
        }
        super.scaleModelForRender(info, widthScale, heightScale);
    }
}
