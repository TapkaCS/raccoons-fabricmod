package com.tapkacs.raccoons.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.tapkacs.raccoons.advancement.ModTriggers;
import com.tapkacs.raccoons.entity.ai.RaccoonBegGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonClimbGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonFollowOwnerGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonLookAtPlayerGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonOpenDoorGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonRandomLookAroundGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonStashGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonStealFoodGoal;
import com.tapkacs.raccoons.entity.ai.RaccoonWanderGoal;
import com.tapkacs.raccoons.item.ModItems;
import com.tapkacs.raccoons.sound.ModSounds;
import com.tapkacs.raccoons.stat.ModStats;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RaccoonEntity extends TamableAnimal implements GeoEntity, ContainerUser {

    public enum BehaviorMode {
        FOLLOW, WANDER, SIT
    }

    public enum ColorVariant {
        NORMAL, ALBINO, MELANISTIC
    }

    private final AnimatableInstanceCache geoCache = new InstancedAnimatableInstanceCache(this);

    private static final String BEHAVIOR_MODE_TAG = "BehaviorMode";

    private static final EntityDataAccessor<Byte> DATA_BEHAVIOR_MODE =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BYTE);

    private static final EntityDataAccessor<Boolean> DATA_SLEEPING =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_BEGGING =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<ItemStack> DATA_CARRIED_ITEM =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final EntityDataAccessor<Boolean> DATA_WASHING =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_DOOR_JUMPING =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_CHUNKY =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String CHUNKY_TAG = "Chunky";
    private static final float CHUNKY_SPAWN_CHANCE = 0.1f;

    private static final EntityDataAccessor<Byte> DATA_COLOR_VARIANT =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BYTE);

    private static final String COLOR_VARIANT_TAG = "ColorVariant";
    private static final float ALBINO_SPAWN_CHANCE = 0.02f;
    private static final float MELANISTIC_SPAWN_CHANCE = 0.02f; // combined 0.04 = 1-in-25 chance of any special coloring

    private static final EntityDataAccessor<Boolean> DATA_CLIMBING =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double CLIMB_SPEED = 0.15;

    private static final String FED_AMOUNT_TAG = "FedAmount";
    private static final int OVERFEED_THRESHOLD = 32; // half a stack of food -> becomes chunky
    private static final int TAMED_RACCOONS_ACHIEVEMENT_THRESHOLD = 50;

    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.INT);
    private static final String COLLAR_COLOR_TAG = "CollarColor";
    private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;

    private static final EntityDataAccessor<Boolean> DATA_HAS_HAT =
            SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String HAS_HAT_TAG = "HasHat";

    // -1 means "not armed yet"; gets rolled to a random 5-25s (100-500 ticks) once the
    // raccoon settles into Sit mode, then counts down to 0 to trigger the sleeping pose.
    private int sleepTimer = -1;

    // Counts food items fed (heal-only, not taming) since the last chunky conversion; resets
    // once it crosses OVERFEED_THRESHOLD and flips the raccoon chunky.
    private int fedAmount = 0;

    // Set by RaccoonStealFoodGoal while it's actually rummaging through a chest, so the
    // chest's lid animation knows this raccoon counts as an opener (see ContainerUser below).
    private BlockPos openedChestPos;

    public RaccoonEntity(EntityType<? extends RaccoonEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Lets pathfinding route the raccoon over fences instead of always detouring around them (same
     * flag vanilla foxes/chickens use), and through closed doors - clever paws can open those
     * (see the {@link RaccoonOpenDoorGoal} in {@link #registerGoals()}), so raids can path into buildings
     * instead of piling up against the outside wall nearest the chest.
     */
    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanWalkOverFences(true);
        navigation.setCanOpenDoors(true);
        return navigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BEHAVIOR_MODE, (byte) BehaviorMode.FOLLOW.ordinal());
        builder.define(DATA_SLEEPING, false);
        builder.define(DATA_BEGGING, false);
        builder.define(DATA_CARRIED_ITEM, ItemStack.EMPTY);
        builder.define(DATA_WASHING, false);
        builder.define(DATA_DOOR_JUMPING, false);
        builder.define(DATA_CHUNKY, false);
        builder.define(DATA_COLOR_VARIANT, (byte) ColorVariant.NORMAL.ordinal());
        builder.define(DATA_CLIMBING, false);
        builder.define(DATA_COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getId());
        builder.define(DATA_HAS_HAT, false);
    }

    public BehaviorMode getBehaviorMode() {
        return BehaviorMode.values()[this.entityData.get(DATA_BEHAVIOR_MODE)];
    }

    public void setBehaviorMode(BehaviorMode mode) {
        this.entityData.set(DATA_BEHAVIOR_MODE, (byte) mode.ordinal());
        this.setOrderedToSit(mode == BehaviorMode.SIT);
        this.setInSittingPose(mode == BehaviorMode.SIT);
        if (mode != BehaviorMode.SIT) {
            this.setSleepingPose(false);
            this.sleepTimer = -1;
        }
    }

    public boolean isSleepingPose() {
        return this.entityData.get(DATA_SLEEPING);
    }

    public void setSleepingPose(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING, sleeping);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);

        if (!this.isTame() || this.getBehaviorMode() != BehaviorMode.SIT) {
            return;
        }

        if (this.isSleepingPose()) {
            return;
        }

        if (this.sleepTimer < 0) {
            this.sleepTimer = 100 + this.random.nextInt(401); // 100-500 ticks = 5-25s
        } else if (this.sleepTimer > 0) {
            this.sleepTimer--;
        } else {
            this.setSleepingPose(true);
        }
    }

    /**
     * Vines/ladders already work for free via vanilla's {@code BlockTags.CLIMBABLE} handling in
     * {@link net.minecraft.world.entity.LivingEntity#onClimbable()}. Logs/leaves/fences aren't
     * tagged climbable, so we mimic how {@code Spider} climbs walls: whenever movement is blocked
     * sideways by one of those blocks, treat it as a climbable surface and nudge the raccoon
     * upward each tick until it clears the obstruction.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        boolean onCustomClimbable = this.horizontalCollision && this.isNextToClimbableBlock();
        this.setClimbing(onCustomClimbable);
        if (onCustomClimbable) {
            Vec3 delta = this.getDeltaMovement();
            this.setDeltaMovement(delta.x, Math.max(delta.y, CLIMB_SPEED), delta.z);
        }
    }

    private boolean isNextToClimbableBlock() {
        BlockPos pos = this.blockPosition();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState state = this.level().getBlockState(pos.relative(direction));
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.FENCES)) {
                return true;
            }
        }
        return false;
    }

    public boolean isClimbing() {
        return this.entityData.get(DATA_CLIMBING);
    }

    private void setClimbing(boolean climbing) {
        this.entityData.set(DATA_CLIMBING, climbing);
    }

    @Override
    public boolean onClimbable() {
        return super.onClimbable() || this.isClimbing();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte(BEHAVIOR_MODE_TAG, (byte) this.getBehaviorMode().ordinal());
        output.putBoolean(CHUNKY_TAG, this.isChunky());
        output.putByte(COLOR_VARIANT_TAG, (byte) this.getColorVariant().ordinal());
        output.putInt(FED_AMOUNT_TAG, this.fedAmount);
        output.putByte(COLLAR_COLOR_TAG, (byte) this.getCollarColor().getId());
        output.putBoolean(HAS_HAT_TAG, this.hasHat());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_BEHAVIOR_MODE, input.getByteOr(BEHAVIOR_MODE_TAG, (byte) BehaviorMode.FOLLOW.ordinal()));
        this.entityData.set(DATA_CHUNKY, input.getBooleanOr(CHUNKY_TAG, false));
        this.entityData.set(DATA_COLOR_VARIANT, input.getByteOr(COLOR_VARIANT_TAG, (byte) ColorVariant.NORMAL.ordinal()));
        this.fedAmount = input.getIntOr(FED_AMOUNT_TAG, 0);
        this.entityData.set(DATA_COLLAR_COLOR, (int) input.getByteOr(COLLAR_COLOR_TAG, (byte) DEFAULT_COLLAR_COLOR.getId()));
        this.entityData.set(DATA_HAS_HAT, input.getBooleanOr(HAS_HAT_TAG, false));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new RaccoonOpenDoorGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new RaccoonStealFoodGoal(this));
        this.goalSelector.addGoal(4, new RaccoonStashGoal(this));
        this.goalSelector.addGoal(5, new RaccoonBegGoal(this, 8.0f));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(7, new RaccoonFollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(8, new RaccoonClimbGoal(this));
        this.goalSelector.addGoal(9, new RaccoonWanderGoal(this, 1.0));
        this.goalSelector.addGoal(10, new RaccoonLookAtPlayerGoal(this, 6.0f));
        this.goalSelector.addGoal(11, new RaccoonRandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        RaccoonEntity baby = ModEntityTypes.RACCOON.create(level, EntitySpawnReason.BREEDING);
        if (baby != null) {
            baby.setTame(true, true);
            baby.setOwnerReference(this.getOwnerReference());
        }
        return baby;
    }

    /** Only tamed raccoons can breed - wild ones never reach {@link Animal#isInLove()} in the first place (see {@link #mobInteract}), but this guards against love mode being set some other way. */
    @Override
    public boolean canMate(Animal otherAnimal) {
        return super.canMate(otherAnimal) && this.isTame()
                && otherAnimal instanceof RaccoonEntity other && other.isTame();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE);
    }

    /**
     * Broader than {@link #isFood(ItemStack)} on purpose: taming stays apple-only, but
     * begging should react to any edible item the player is holding.
     */
    public boolean isInterestingFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    public boolean isBegging() {
        return this.entityData.get(DATA_BEGGING);
    }

    public void setBegging(boolean begging) {
        this.entityData.set(DATA_BEGGING, begging);
    }

    /** Item shown clipped to the "snout" bone by {@link com.tapkacs.raccoons.client.entity.RaccoonMouthItemGeoLayer} while stealing/washing food. */
    public ItemStack getCarriedItem() {
        return this.entityData.get(DATA_CARRIED_ITEM);
    }

    public void setCarriedItem(ItemStack stack) {
        this.entityData.set(DATA_CARRIED_ITEM, stack);
    }

    public boolean isWashing() {
        return this.entityData.get(DATA_WASHING);
    }

    public void setWashing(boolean washing) {
        this.entityData.set(DATA_WASHING, washing);
    }

    /** Standing-on-hind-legs jump played while working a door open (see {@link com.tapkacs.raccoons.entity.ai.RaccoonOpenDoorGoal}). */
    public boolean isDoorJumping() {
        return this.entityData.get(DATA_DOOR_JUMPING);
    }

    public void setDoorJumping(boolean doorJumping) {
        this.entityData.set(DATA_DOOR_JUMPING, doorJumping);
    }

    /** Purely visual/hitbox-cosmetic "big" variant; picks a different GeckoLib model in {@link com.tapkacs.raccoons.client.entity.RaccoonGeoModel}. */
    public boolean isChunky() {
        return this.entityData.get(DATA_CHUNKY);
    }

    public void setChunky(boolean chunky) {
        this.entityData.set(DATA_CHUNKY, chunky);
    }

    /** Purely visual "rare coloring" variant; picks a different texture in {@link com.tapkacs.raccoons.client.entity.RaccoonGeoModel}. */
    public ColorVariant getColorVariant() {
        return ColorVariant.values()[this.entityData.get(DATA_COLOR_VARIANT)];
    }

    public void setColorVariant(ColorVariant variant) {
        this.entityData.set(DATA_COLOR_VARIANT, (byte) variant.ordinal());
    }

    /** Band color of the hat, once equipped; dyed via right-click with any dye item. */
    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor color) {
        this.entityData.set(DATA_COLLAR_COLOR, color.getId());
    }

    /** Whether the hat item has been equipped onto this raccoon (right-click with the hat item while tamed). */
    public boolean hasHat() {
        return this.entityData.get(DATA_HAS_HAT);
    }

    public void setHasHat(boolean hasHat) {
        this.entityData.set(DATA_HAS_HAT, hasHat);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.setChunky(this.random.nextFloat() < CHUNKY_SPAWN_CHANCE);

        float colorRoll = this.random.nextFloat();
        if (colorRoll < ALBINO_SPAWN_CHANCE) {
            this.setColorVariant(ColorVariant.ALBINO);
        } else if (colorRoll < ALBINO_SPAWN_CHANCE + MELANISTIC_SPAWN_CHANCE) {
            this.setColorVariant(ColorVariant.MELANISTIC);
        }

        return result;
    }

    public void setOpenedChestPos(BlockPos pos) {
        this.openedChestPos = pos;
    }

    @Override
    public boolean hasContainerOpen(ContainerOpenersCounter counter, BlockPos pos) {
        return this.isAlive() && pos.equals(this.openedChestPos);
    }

    @Override
    public double getContainerInteractionRange() {
        return 4.0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RACCOON_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.RACCOON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0);
            this.setHealth(60.0f);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!this.isTame() && this.isFood(itemstack)) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem(player, hand, itemstack);
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.setBehaviorMode(BehaviorMode.SIT);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    this.awardTamedRaccoonProgress(player);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Apple on an already-tamed raccoon: breeding trigger, not another taming attempt.
        if (this.isTame() && this.isFood(itemstack) && this.canFallInLove() && !this.isInLove()) {
            if (!this.level().isClientSide()) {
                this.feed(player, hand, itemstack, 1.0F, 4.0F);
                this.setInLove(player);
                this.trackOverfeeding(player);
                // SitWhenOrderedToGoal outranks BreedGoal and shares its MOVE flag, so a sitting
                // raccoon would never actually walk over to a partner - let it off the leash.
                if (this.getBehaviorMode() == BehaviorMode.SIT) {
                    this.setBehaviorMode(BehaviorMode.WANDER);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Any other food, on any raccoon: just feed/heal it, no taming or breeding side effects.
        if (this.isInterestingFood(itemstack)) {
            if (!this.level().isClientSide()) {
                this.feed(player, hand, itemstack, 1.0F, 4.0F);
                this.trackOverfeeding(player);
            }
            return InteractionResult.SUCCESS;
        }

        // The hat item on an owned, tamed raccoon: equip the hat. Deliberately NOT shift-click -
        // that combo is claimed by mob-collector mods (e.g. picks the raccoon up instead of reaching this code).
        if (this.isTame() && this.isOwnedBy(player) && itemstack.is(ModItems.RACCOON_HAT) && !this.hasHat()) {
            if (!this.level().isClientSide()) {
                this.setHasHat(true);
                itemstack.consume(1, player);
                if (player instanceof ServerPlayer serverPlayer) {
                    ModTriggers.EQUIPPED_HAT.trigger(serverPlayer);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Any dye item on an owned, tamed raccoon wearing the hat: re-color its band.
        if (this.isTame() && this.isOwnedBy(player) && this.hasHat() && itemstack.has(DataComponents.DYE)) {
            DyeColor color = itemstack.get(DataComponents.DYE);
            if (!this.level().isClientSide() && color != this.getCollarColor()) {
                this.setCollarColor(color);
                itemstack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }

        // The bell: don't let clicking a raccoon while holding it get swallowed by the mode-cycling catch-all below -
        // let it fall through to the bell's own use()/useOn() ring logic instead.
        if (itemstack.is(ModItems.RACCOON_BELL)) {
            return InteractionResult.PASS;
        }

        if (this.isTame() && this.isOwnedBy(player) && !this.isFood(itemstack)) {
            if (!this.level().isClientSide()) {
                this.cycleBehaviorMode(player);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    /** Half a stack of food (any raccoon, tame or wild) flips it to the chunky variant. */
    private void trackOverfeeding(Player player) {
        if (this.isChunky()) {
            return;
        }
        if (++this.fedAmount >= OVERFEED_THRESHOLD) {
            this.fedAmount = 0;
            this.setChunky(true);
            if (player instanceof ServerPlayer serverPlayer) {
                ModTriggers.BECAME_CHUNKY.trigger(serverPlayer);
            }
        }
    }

    private void awardTamedRaccoonProgress(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.awardStat(ModStats.TAMED_RACCOONS);
        if (serverPlayer.getStats().getValue(Stats.CUSTOM.get(ModStats.TAMED_RACCOONS)) >= TAMED_RACCOONS_ACHIEVEMENT_THRESHOLD) {
            ModTriggers.TAMED_ENOUGH_RACCOONS.trigger(serverPlayer);
        }
    }

    private void cycleBehaviorMode(Player player) {
        BehaviorMode next = switch (this.getBehaviorMode()) {
            case FOLLOW -> BehaviorMode.WANDER;
            case WANDER -> BehaviorMode.SIT;
            case SIT -> BehaviorMode.FOLLOW;
        };
        this.setBehaviorMode(next);
        player.sendOverlayMessage(Component.translatable("entity.raccoons.raccoon.mode." + next.name().toLowerCase()));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<RaccoonEntity>("movement", 5, state -> {
            RaccoonEntity raccoon = state.animatable();
            if (raccoon.isSleepingPose()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("Sleeping"));
            }
            if (raccoon.isWashing()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("Washing"));
            }
            if (raccoon.isDoorJumping()) {
                // Must outrank the isMoving branch - the raccoon is usually mid-path when it stops to work a door.
                state.setControllerSpeed(1.0f);
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("Jumping"));
            }
            if (raccoon.isBegging()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("Begging"));
            }
            if (state.isMoving()) {
                float partialTick = state.renderState().getPartialTick();
                state.setControllerSpeed(raccoon.walkAnimation.speed(partialTick));
                return state.setAndContinue(RawAnimation.begin().thenLoop("Walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("Idle"));
        }));
    }
}