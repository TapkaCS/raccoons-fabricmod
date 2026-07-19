package com.tapkacs.raccoons.client.entity;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** Renders {@link RaccoonEntity#getCarriedItem()} clipped to the "snout" bone, for the steal/wash/eat behavior. */
public class RaccoonMouthItemGeoLayer extends BlockAndItemGeoLayer<RaccoonEntity, Void, RaccoonRenderState> {

    private static final String MOUTH_BONE = "snout";

    public RaccoonMouthItemGeoLayer(EntityRendererProvider.Context context, GeoRenderer<RaccoonEntity, Void, RaccoonRenderState> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(RaccoonEntity animatable, @Nullable Void relatedObject, RaccoonRenderState renderState, float partialTick) {
        ItemStack stack = animatable.getCarriedItem();
        if (stack.isEmpty()) {
            return List.of();
        }

        return List.of(RenderData.item(MOUTH_BONE, ItemDisplayContext.GROUND,
                RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, ItemDisplayContext.GROUND, animatable)));
    }

    @Override
    public void addRenderData(RaccoonEntity animatable, @Nullable Void relatedObject, RaccoonRenderState renderState, float partialTick) {
        List<RenderData> contents = this.getRelevantBones(animatable, relatedObject, renderState, partialTick);

        if (!contents.isEmpty()) {
            renderState.addGeckolibData(CONTENTS, contents);
        }
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState stackState, ItemDisplayContext displayContext, RaccoonRenderState renderState, SubmitNodeCollector renderTasks, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 0.06, -0.08);
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        poseStack.popPose();
    }
}
