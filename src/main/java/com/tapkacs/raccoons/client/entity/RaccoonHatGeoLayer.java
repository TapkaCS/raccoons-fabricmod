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
import org.jspecify.annotations.Nullable;

/** Renders the "hat" bone (the straw hat body, only present once the hat item has been equipped) with its own dedicated texture. */
public class RaccoonHatGeoLayer extends CustomBoneTextureGeoLayer<RaccoonEntity, Void, RaccoonRenderState> {
    private static final DataTicket<Boolean> SHOW_HAT = DataTicket.create("raccoons_show_hat", Boolean.class);

    public RaccoonHatGeoLayer(GeoRenderer<RaccoonEntity, Void, RaccoonRenderState> renderer) {
        super(renderer, "hat", Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "textures/entity/hat.png"));
    }

    @Override
    public boolean shouldRenderBone(RaccoonRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(SHOW_HAT, false);
    }

    @Override
    public void addRenderData(RaccoonEntity animatable, @Nullable Void relatedObject, RaccoonRenderState renderState, float partialTick) {
        renderState.addGeckolibData(SHOW_HAT, animatable.hasHat() && !animatable.isBaby());
    }

    /**
     * Always hide this bone from the default full-body pass (unlike the base class, which only hides it while
     * {@link #shouldRenderBone} is true) - otherwise an untamed raccoon would render the hat's geometry using
     * the raccoon's own body texture at whatever pixels the hat's UV happens to land on.
     */
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
