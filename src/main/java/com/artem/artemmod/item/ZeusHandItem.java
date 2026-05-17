package com.artem.artemmod.item;

import com.artem.artemmod.event.ZeusWatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ZeusHandItem extends Item {
    private static final int COOLDOWN_TICKS = 40;
    private static final int DARKNESS_TICKS = 100;
    private static final float HORROR_CHANCE = 0.12F;
    private static final float CHICKEN_CREEPER_CHANCE = 0.03F;
    private static final float ZEUS_WATCHER_CHANCE = 0.06F;

    public ZeusHandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        BlockPos targetPos;
        if (hit.getType() == HitResult.Type.BLOCK) {
            targetPos = hit.getBlockPos().relative(hit.getDirection());
        } else {
            targetPos = player.blockPosition().relative(player.getDirection(), 10);
        }

        summonLightning(serverLevel, player, targetPos);
        rollCursedEffect(serverLevel, player, targetPos);
        rollZeusWatcher(serverLevel, player);

        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.hurtAndBreak(
                1,
                player,
                player.getEquipmentSlotForItem(stack)
        );

        return InteractionResult.SUCCESS;
    }

    private void summonLightning(ServerLevel serverLevel, Player player, BlockPos targetPos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (lightning == null) {
            return;
        }

        lightning.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);

        if (player instanceof ServerPlayer serverPlayer) {
            lightning.setCause(serverPlayer);
        }

        serverLevel.addFreshEntity(lightning);
    }

    private void rollCursedEffect(ServerLevel serverLevel, Player player, BlockPos targetPos) {
        if (serverLevel.random.nextFloat() < CHICKEN_CREEPER_CHANCE) {
            summonZeusChickenAndChargedCreeper(serverLevel, player, targetPos);
            return;
        }

        if (serverLevel.random.nextFloat() < HORROR_CHANCE) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0));
            player.displayClientMessage(Component.literal("Он смотрит сверху."), false);
        }
    }

    private void rollZeusWatcher(ServerLevel serverLevel, Player player) {
        if (player instanceof ServerPlayer serverPlayer && serverLevel.random.nextFloat() < ZEUS_WATCHER_CHANCE) {
            ZeusWatcherManager.trySpawnWatcher(serverLevel, serverPlayer);
        }
    }

    private void summonZeusChickenAndChargedCreeper(ServerLevel serverLevel, Player player, BlockPos targetPos) {
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0));
        player.displayClientMessage(Component.literal("Это был не гром."), false);

        Chicken chicken = EntityType.CHICKEN.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (chicken != null) {
            chicken.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            chicken.setCustomName(Component.literal("Зевс?"));
            chicken.setCustomNameVisible(true);
            serverLevel.addFreshEntity(chicken);
            summonLightning(serverLevel, player, chicken.blockPosition());
        }

        Creeper creeper = EntityType.CREEPER.create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (creeper != null) {
            creeper.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            serverLevel.addFreshEntity(creeper);
            summonLightning(serverLevel, player, creeper.blockPosition());
        }
    }
}
