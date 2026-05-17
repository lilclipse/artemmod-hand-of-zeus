package com.artem.artemmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ZeusWatcherManager {
    private static final int MAX_LIFETIME_TICKS = 20 * 18;
    private static final double WATCH_DISTANCE = 38.0;
    private static final double LOOK_DOT_THRESHOLD = 0.975;

    private static final Map<UUID, WatcherData> WATCHERS = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, WatcherData>> iterator = WATCHERS.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, WatcherData> entry = iterator.next();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                WatcherData data = entry.getValue();

                if (player == null || data.armorStand.isRemoved()) {
                    removeWatcher(data, false);
                    iterator.remove();
                    continue;
                }

                data.age++;
                if (data.age > MAX_LIFETIME_TICKS) {
                    removeWatcher(data, true);
                    iterator.remove();
                    continue;
                }

                lookAtPlayer(data.armorStand, player);

                if (isPlayerLookingAt(player, data.armorStand)) {
                    ServerLevel level = (ServerLevel) player.level();
                    vanish(level, player, data.armorStand);
                    iterator.remove();
                }
            }
        });
    }

    public static void trySpawnWatcher(ServerLevel level, ServerPlayer player) {
        if (WATCHERS.containsKey(player.getUUID())) {
            return;
        }

        Vec3 look = player.getLookAngle().normalize();
        Vec3 spawnCenter = player.position()
                .subtract(look.scale(8.0))
                .add((level.random.nextDouble() - 0.5) * 4.0, 0.0, (level.random.nextDouble() - 0.5) * 4.0);

        BlockPos spawnPos = BlockPos.containing(spawnCenter);
        ArmorStand armorStand = EntityType.ARMOR_STAND.create(level, EntitySpawnReason.TRIGGERED);
        if (armorStand == null) {
            return;
        }

        armorStand.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        armorStand.setCustomName(Component.literal("Зевс"));
        armorStand.setCustomNameVisible(false);
        armorStand.setNoGravity(true);
        armorStand.setInvulnerable(true);
        armorStand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        armorStand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
        armorStand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.LIGHTNING_ROD));

        level.addFreshEntity(armorStand);
        WATCHERS.put(player.getUUID(), new WatcherData(armorStand));
    }

    private static boolean isPlayerLookingAt(ServerPlayer player, ArmorStand armorStand) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = armorStand.position().add(0.0, armorStand.getBbHeight() * 0.65, 0.0);
        Vec3 toWatcher = targetPos.subtract(eyePos);
        double distance = toWatcher.length();

        if (distance > WATCH_DISTANCE) {
            return false;
        }

        Vec3 look = player.getLookAngle().normalize();
        Vec3 direction = toWatcher.normalize();
        return look.dot(direction) > LOOK_DOT_THRESHOLD;
    }

    private static void lookAtPlayer(ArmorStand armorStand, ServerPlayer player) {
        Vec3 direction = player.position().subtract(armorStand.position());
        double yaw = Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0;
        armorStand.setYRot((float) yaw);
        armorStand.setYHeadRot((float) yaw);
    }

    private static void vanish(ServerLevel level, ServerPlayer player, ArmorStand armorStand) {
        Vec3 pos = armorStand.position();
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 1.0, pos.z, 35, 0.45, 0.7, 0.45, 0.025);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0, pos.z, 25, 0.35, 0.55, 0.35, 0.05);
        level.playSound(null, armorStand.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.55F);
        player.displayClientMessage(Component.literal("Ты видел не того бога."), false);
        armorStand.discard();
    }

    private static void removeWatcher(WatcherData data, boolean discard) {
        if (discard && !data.armorStand.isRemoved()) {
            data.armorStand.discard();
        }
    }

    private static class WatcherData {
        private final ArmorStand armorStand;
        private int age;

        private WatcherData(ArmorStand armorStand) {
            this.armorStand = armorStand;
            this.age = 0;
        }
    }
}
