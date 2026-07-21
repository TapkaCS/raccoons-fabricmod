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
    private final Identifier babyAlbinoTextureResource;
    private final Identifier babyMelanisticTextureResource;
    private final Identifier babyAnimationResource;
    private final Identifier chunkyAnimationResource;
    private final Identifier chunkyTextureResource;
    private final Identifier chunkyAlbinoTextureResource;
    private final Identifier chunkyMelanisticTextureResource;

    public RaccoonGeoModel() {
        super(ModEntityTypes.RACCOON);
        this.chunkyModelResource = buildFormattedModelPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky"));
        this.albinoTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_albino"));
        this.melanisticTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_melanistic"));
        this.babyModelResource = buildFormattedModelPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby"));
        this.babyTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby"));
        this.babyAlbinoTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby_albino"));
        this.babyMelanisticTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby_melanistic"));
        this.babyAnimationResource = buildFormattedAnimationPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby"));
        this.chunkyAnimationResource = buildFormattedAnimationPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky"));
        this.chunkyTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky"));
        this.chunkyAlbinoTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky_albino"));
        this.chunkyMelanisticTextureResource = buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky_melanistic"));
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
        RaccoonEntity.ColorVariant variant =
                renderState.getOrDefaultGeckolibData(COLOR_VARIANT, RaccoonEntity.ColorVariant.NORMAL);
        if (renderState.getOrDefaultGeckolibData(BABY, false)) {
            return switch (variant) {
                case ALBINO -> this.babyAlbinoTextureResource;
                case MELANISTIC -> this.babyMelanisticTextureResource;
                case NORMAL -> this.babyTextureResource;
            };
        }
        if (renderState.getOrDefaultGeckolibData(CHUNKY, false)) {
            return switch (variant) {
                case ALBINO -> this.chunkyAlbinoTextureResource;
                case MELANISTIC -> this.chunkyMelanisticTextureResource;
                case NORMAL -> this.chunkyTextureResource;
            };
        }
        return switch (variant) {
            case ALBINO -> this.albinoTextureResource;
            case MELANISTIC -> this.melanisticTextureResource;
            case NORMAL -> super.getTextureResource(renderState);
        };
    }

    // Baby and chunky each have their own animation set (delivered inside their bbmodels) tuned
    // to their model's pivots - the adult clips would bend the resized bones around wrong points.
    @Override
    public Identifier getAnimationResource(RaccoonEntity animatable) {
        if (animatable.isBaby()) {
            return this.babyAnimationResource;
        }
        if (animatable.isChunky()) {
            return this.chunkyAnimationResource;
        }
        return super.getAnimationResource(animatable);
    }
}
