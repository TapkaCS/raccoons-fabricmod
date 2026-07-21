package com.tapkacs.raccoons.client.entity;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.tapkacs.raccoons.entity.ModEntityTypes;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class RaccoonGeoModel extends DefaultedEntityGeoModel<RaccoonEntity> {
    private static final DataTicket<Boolean> CHUNKY = DataTicket.create("raccoons_chunky", Boolean.class);
    private static final DataTicket<RaccoonEntity.ColorVariant> COLOR_VARIANT =
            DataTicket.create("raccoons_color_variant", RaccoonEntity.ColorVariant.class);
    private static final DataTicket<Boolean> BABY = DataTicket.create("raccoons_baby", Boolean.class);

    private final Identifier chunkyModelResource;
    private final Identifier babyModelResource;
    private final Identifier babyAnimationResource;
    private final Identifier chunkyAnimationResource;

    // Texture per color variant, one map per body form. Files follow a strict naming scheme:
    // raccoon[_baby|_chunky][_<variant-lowercase>].png - a new variant only needs enum + textures.
    private final Map<RaccoonEntity.ColorVariant, Identifier> adultTextures = new EnumMap<>(RaccoonEntity.ColorVariant.class);
    private final Map<RaccoonEntity.ColorVariant, Identifier> babyTextures = new EnumMap<>(RaccoonEntity.ColorVariant.class);
    private final Map<RaccoonEntity.ColorVariant, Identifier> chunkyTextures = new EnumMap<>(RaccoonEntity.ColorVariant.class);

    public RaccoonGeoModel() {
        super(ModEntityTypes.RACCOON);
        this.chunkyModelResource = buildFormattedModelPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky"));
        this.babyModelResource = buildFormattedModelPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby"));
        this.babyAnimationResource = buildFormattedAnimationPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_baby"));
        this.chunkyAnimationResource = buildFormattedAnimationPath(Identifier.fromNamespaceAndPath("raccoons", "raccoon_chunky"));

        for (RaccoonEntity.ColorVariant variant : RaccoonEntity.ColorVariant.values()) {
            String suffix = variant == RaccoonEntity.ColorVariant.NORMAL
                    ? "" : "_" + variant.name().toLowerCase(Locale.ROOT);
            this.adultTextures.put(variant, texture("raccoon" + suffix));
            this.babyTextures.put(variant, texture("raccoon_baby" + suffix));
            this.chunkyTextures.put(variant, texture("raccoon_chunky" + suffix));
        }
    }

    private Identifier texture(String name) {
        return buildFormattedTexturePath(Identifier.fromNamespaceAndPath("raccoons", name));
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
            return this.babyTextures.get(variant);
        }
        if (renderState.getOrDefaultGeckolibData(CHUNKY, false)) {
            return this.chunkyTextures.get(variant);
        }
        return this.adultTextures.get(variant);
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
