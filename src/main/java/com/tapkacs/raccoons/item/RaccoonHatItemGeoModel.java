package com.tapkacs.raccoons.item;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.tapkacs.raccoons.Raccoons;
import net.minecraft.resources.Identifier;

/** Reuses the same straw/band textures as the equipped-on-head render, but with its own standalone (re-centered) geometry for held/GUI display. */
public class RaccoonHatItemGeoModel extends GeoModel<RaccoonHatItem> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "item/raccoon_hat");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "textures/entity/hat.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "entity/raccoon");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(RaccoonHatItem animatable) {
        return ANIMATION;
    }
}
