package com.tapkacs.raccoons.item;

import com.geckolib.constant.DataTickets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.tapkacs.raccoons.Raccoons;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public class RaccoonBellItemGeoModel extends GeoModel<RaccoonBellItem> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "item/raccoon_bell");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "textures/item/raccoon_bell_3d.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "item/raccoon_bell");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(RaccoonBellItem animatable) {
        return ANIMATION;
    }

    @Override
    public void addAdditionalStateData(RaccoonBellItem animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        boolean moving = false;
        if (relatedObject instanceof GeoItemRenderer.RenderData data && data.itemOwner() != null) {
            LivingEntity holder = data.itemOwner().asLivingEntity();
            moving = holder != null && holder.walkAnimation.isMoving();
        }
        renderState.addGeckolibData(DataTickets.IS_MOVING, moving);
    }
}
