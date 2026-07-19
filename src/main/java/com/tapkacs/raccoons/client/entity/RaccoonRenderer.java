package com.tapkacs.raccoons.client.entity;

import com.geckolib.renderer.GeoEntityRenderer;
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
}
