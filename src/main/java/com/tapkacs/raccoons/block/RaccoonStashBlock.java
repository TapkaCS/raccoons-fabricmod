package com.tapkacs.raccoons.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A hollowed-out tree trunk wild raccoons stash stolen loot in. Opens like a small chest. */
public class RaccoonStashBlock extends BaseEntityBlock {
    public static final MapCodec<RaccoonStashBlock> CODEC = simpleCodec(RaccoonStashBlock::new);

    public RaccoonStashBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<RaccoonStashBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RaccoonStashBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RaccoonStashBlockEntity stash) {
            player.openMenu(stash);
        }
        return InteractionResult.SUCCESS;
    }
}
