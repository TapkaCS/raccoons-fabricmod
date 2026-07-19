package com.tapkacs.raccoons.client.entity;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.tapkacs.raccoons.entity.ModEntityTypes;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.resources.Identifier;

public class RaccoonGeoModel extends DefaultedEntityGeoModel<RaccoonEntity> {
    private static final DataTicket<Boolean> CHUNKY = DataTicket.create("raccoons_chunky", Boolean.class);
    private static final DataTicket<RaccoonEntity.ColorVariant> COLOR_VARIANT =
            DataTicket.create("raccoons_color_variant", RaccoonEntity.ColorVariant.class);
    private static final DataTicket<Boolean> BABY = DataTicket.create("raccoons_baby", Boolean.class);

    private final Identifier chunkyModelResource;
    private final Identifier albinoTextureResource;
    private final Identifier melanisticTextureResource;
    private final Identifier babyModelResource;
    private final Identifier babyTextureResource;

    public RaccoonGeoModel() {
        super(ModEntityTypes.RACCOON);
        this.chunkyModelResource = buildFormattedModelPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky"));
        this.albinoTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_albino"));
        this.melanisticTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_melanistic"));
        this.babyModelResource = buildFormattedModelPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby"));
        this.babyTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby"));
    }

    @Override
    public void addAdditionalStateData(RaccoonEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(CHUNKY, animatable.isChunky());
        renderState.addGeckolibData(COLOR_VARIANT, animatable.getColorVariant());
        renderState.addGeckolibData(BABY, animatable.isBaby());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(BABY, false)) {
            return this.babyModelResource;
        }
        return renderState.getOrDefaultGeckolibData(CHUNKY, false) ? this.chunkyModelResource : super.getModelResource(renderState);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(BABY, false)) {
            return this.babyTextureResource;
        }
        return switch (renderState.getOrDefaultGeckolibData(COLOR_VARIANT, RaccoonEntity.ColorVariant.NORMAL)) {
            case ALBINO -> this.albinoTextureResource;
            case MELANISTIC -> this.melanisticTextureResource;
            case NORMAL -> super.getTextureResource(renderState);
        };
    }
}
