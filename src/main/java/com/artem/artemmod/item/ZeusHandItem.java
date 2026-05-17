package com.artem.artemmod.item;

import com.artem.artemmod.event.ZeusWatcherManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZeusHandItem extends Item {
    private static final int COOLDOWN_TICKS = 40;
    private static final int DARKNESS_TICKS = 100;

    private static final float HORROR_CHANCE = 0.12F;
    private static final float CHICKEN_CREEPER_CHANCE = 0.03F;
    private static final float ZEUS_WATCHER_CHANCE = 0.15F;
    private static final float DARKNESS_WITH_ZEUS_CHANCE = 0.08F;
    private static final float ZEUS_IN_DARKNESS_CHANCE = 0.75F;

    private static final float FAKE_JOIN_CHANCE = 0.04F;
    private static final float WHISPER_CHANCE = 0.07F;
    private static final float DELAYED_WATCHER_CHANCE = 0.05F;

    private static final Map<UUID, Integer> USE_DEBT = new HashMap<>();

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
        rollDarknessWithZeusScenario(serverLevel, player);
        rollFakeJoinScenario(serverLevel, player);
        rollWhisperScenario(serverLevel, player);
        rollDelayedWatcherScenario(serverLevel, player);
        updateDebtCounter(player);

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
            applyDarknessMessageAndLaugh(serverLevel, player);
        }
    }

    private void rollZeusWatcher(ServerLevel serverLevel, Player player) {
        if (player instanceof ServerPlayer serverPlayer && serverLevel.random.nextFloat() < ZEUS_WATCHER_CHANCE) {
            ZeusWatcherManager.trySpawnWatcher(serverLevel, serverPlayer);
        }
    }

    private void rollDarknessWithZeusScenario(ServerLevel serverLevel, Player player) {
        if (serverLevel.random.nextFloat() >= DARKNESS_WITH_ZEUS_CHANCE) {
            return;
        }

        applyDarknessMessageAndLaugh(serverLevel, player);

        if (player instanceof ServerPlayer serverPlayer && serverLevel.random.nextFloat() < ZEUS_IN_DARKNESS_CHANCE) {
            ZeusWatcherManager.trySpawnWatcher(serverLevel, serverPlayer);
        }
    }

    private void rollFakeJoinScenario(ServerLevel serverLevel, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverLevel.random.nextFloat() >= FAKE_JOIN_CHANCE) {
            return;
        }

        serverPlayer.displayClientMessage(Component.literal("Zeus joined the game"), false);
        ZeusWatcherManager.scheduleMessage(serverPlayer, 20 * 4, Component.literal("Zeus left the game"));
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.55F, 0.55F);
    }

    private void rollWhisperScenario(ServerLevel serverLevel, Player player) {
        if (serverLevel.random.nextFloat() >= WHISPER_CHANCE) {
            return;
        }

        String[] whispers = {
                "...",
                "тише",
                "не смотри вверх",
                "он уже здесь"
        };

        player.displayClientMessage(Component.literal(whispers[serverLevel.random.nextInt(whispers.length)]), false);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.HOSTILE, 0.65F, 0.6F);
    }

    private void rollDelayedWatcherScenario(ServerLevel serverLevel, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverLevel.random.nextFloat() >= DELAYED_WATCHER_CHANCE) {
            return;
        }

        ZeusWatcherManager.scheduleMessage(serverPlayer, 20 * 3, Component.literal("Поздно."));
        ZeusWatcherManager.scheduleWatcher(serverPlayer, 20 * 4);
    }

    private void updateDebtCounter(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int uses = USE_DEBT.getOrDefault(serverPlayer.getUUID(), 0) + 1;
        USE_DEBT.put(serverPlayer.getUUID(), uses);

        if (uses == 10) {
            serverPlayer.displayClientMessage(Component.literal("Долг замечен."), false);
        } else if (uses == 20) {
            serverPlayer.displayClientMessage(Component.literal("Долг почти уплачен."), false);
        } else if (uses == 30) {
            serverPlayer.displayClientMessage(Component.literal("Он идет."), false);
        }
    }

    private void applyDarknessMessageAndLaugh(ServerLevel serverLevel, Player player) {
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_TICKS, 0));
        player.displayClientMessage(Component.literal("Он смотрит сверху."), false);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.WITCH_AMBIENT, SoundSource.HOSTILE, 0.9F, 0.6F);
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
