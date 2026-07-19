package com.tapkacs.raccoons.client.entity;

import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;
import com.tapkacs.raccoons.Raccoons;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/** Renders the "prouzek" bone (the hat's dyeable band, only present once the hat item has been equipped) with a solid-color texture matching {@link RaccoonEntity#getCollarColor()}. */
public class RaccoonHatBandGeoLayer extends CustomBoneTextureGeoLayer<RaccoonEntity, Void, RaccoonRenderState> {
    private static final DataTicket<Boolean> SHOW_HAT = DataTicket.create("raccoons_show_hat", Boolean.class);
    private static final DataTicket<DyeColor> COLLAR_COLOR = DataTicket.create("raccoons_collar_color", DyeColor.class);

    private static final Map<DyeColor, Identifier> BAND_TEXTURES = buildTextureMap();

    public RaccoonHatBandGeoLayer(GeoRenderer<RaccoonEntity, Void, RaccoonRenderState> renderer) {
        super(renderer, "prouzek", BAND_TEXTURES.get(DyeColor.RED));
    }

    private static Map<DyeColor, Identifier> buildTextureMap() {
        Map<DyeColor, Identifier> map = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            map.put(color, Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "textures/entity/collar/" + color.getSerializedName() + ".png"));
        }
        return map;
    }

    @Override
    public boolean shouldRenderBone(RaccoonRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(SHOW_HAT, false);
    }

    @Override
    public void addRenderData(RaccoonEntity animatable, @Nullable Void relatedObject, RaccoonRenderState renderState, float partialTick) {
        renderState.addGeckolibData(SHOW_HAT, animatable.hasHat() && !animatable.isBaby());
        renderState.addGeckolibData(COLLAR_COLOR, animatable.getCollarColor());
    }

    @Override
    protected Identifier getTextureResource(RaccoonRenderState renderState) {
        return BAND_TEXTURES.get(renderState.getOrDefaultGeckolibData(COLLAR_COLOR, DyeColor.RED));
    }

    /** See {@link RaccoonHatGeoLayer#preRender} for why this always hides the bone from the default pass. */
    @Override
    public void preRender(RenderPassInfo<RaccoonRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        if (renderPassInfo.willRender()) {
            renderPassInfo.addBoneUpdater((info, snapshots) -> snapshots.get(this.boneName)
                    .filter(snapshot -> snapshot.getBone() instanceof CuboidGeoBone)
                    .ifPresent(snapshot -> {
                        boolean skipChildren = snapshot.areChildrenHidden();
                        snapshot.skipRender(true);
                        snapshot.skipChildrenRender(skipChildren);
                    }));
        }
    }
}
