package com.artem.artemmod.event;

import com.artem.artemmod.entity.ModEntities;
import com.artem.artemmod.entity.ZeusWatcherEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZeusWatcherManager {
    private static final int MAX_LIFETIME_TICKS = 20 * 18;
    private static final double WATCH_DISTANCE = 38.0;
    private static final double LOOK_DOT_THRESHOLD = 0.975;

    private static final String[] VANISH_MESSAGES = {
            "Ты видел не того бога.",
            "Он стоял там дольше, чем ты думаешь.",
            "Не оборачивайся второй раз.",
            "Небо запомнило твое лицо.",
            "Ты заметил его. Он заметил это.",
            "Гром не всегда приходит сверху.",
            "Он ушел. Но тень осталась.",
            "Следующий взгляд будет последним предупреждением.",
            "Ты не должен был поднимать глаза.",
            "Молния была приманкой."
    };

    private static final Map<UUID, WatcherData> WATCHERS = new HashMap<>();
    private static final Map<UUID, List<ScheduledMessage>> SCHEDULED_MESSAGES = new HashMap<>();
    private static final Map<UUID, List<ScheduledWatcher>> SCHEDULED_WATCHERS = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            tickScheduledMessages(players);
            tickScheduledWatchers(players);

            Iterator<Map.Entry<UUID, WatcherData>> iterator = WATCHERS.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, WatcherData> entry = iterator.next();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                WatcherData data = entry.getValue();

                if (player == null || data.zeus.isRemoved()) {
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

                lookAtPlayer(data.zeus, player);

                if (isPlayerLookingAt(player, data.zeus)) {
                    ServerLevel level = (ServerLevel) player.level();
                    vanish(level, player, data.zeus);
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
        ZeusWatcherEntity zeus = ModEntities.ZEUS_WATCHER.create(level, EntitySpawnReason.TRIGGERED);
        if (zeus == null) {
            return;
        }

        zeus.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        zeus.setCustomName(Component.literal("Зевс"));
        zeus.setCustomNameVisible(false);
        zeus.setNoGravity(true);
        zeus.setInvulnerable(true);

        level.addFreshEntity(zeus);
        WATCHERS.put(player.getUUID(), new WatcherData(zeus));
    }

    public static void scheduleMessage(ServerPlayer player, int delayTicks, Component message) {
        SCHEDULED_MESSAGES
                .computeIfAbsent(player.getUUID(), uuid -> new ArrayList<>())
                .add(new ScheduledMessage(delayTicks, message));
    }

    public static void scheduleWatcher(ServerPlayer player, int delayTicks) {
        SCHEDULED_WATCHERS
                .computeIfAbsent(player.getUUID(), uuid -> new ArrayList<>())
                .add(new ScheduledWatcher(delayTicks));
    }

    private static void tickScheduledMessages(List<ServerPlayer> players) {
        if (SCHEDULED_MESSAGES.isEmpty()) {
            return;
        }

        Map<UUID, ServerPlayer> onlinePlayers = mapPlayers(players);
        Iterator<Map.Entry<UUID, List<ScheduledMessage>>> mapIterator = SCHEDULED_MESSAGES.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<UUID, List<ScheduledMessage>> entry = mapIterator.next();
            ServerPlayer player = onlinePlayers.get(entry.getKey());

            if (player == null) {
                mapIterator.remove();
                continue;
            }

            Iterator<ScheduledMessage> messageIterator = entry.getValue().iterator();
            while (messageIterator.hasNext()) {
                ScheduledMessage scheduledMessage = messageIterator.next();
                scheduledMessage.delayTicks--;

                if (scheduledMessage.delayTicks <= 0) {
                    player.displayClientMessage(scheduledMessage.message, false);
                    messageIterator.remove();
                }
            }

            if (entry.getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    private static void tickScheduledWatchers(List<ServerPlayer> players) {
        if (SCHEDULED_WATCHERS.isEmpty()) {
            return;
        }

        Map<UUID, ServerPlayer> onlinePlayers = mapPlayers(players);
        Iterator<Map.Entry<UUID, List<ScheduledWatcher>>> mapIterator = SCHEDULED_WATCHERS.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<UUID, List<ScheduledWatcher>> entry = mapIterator.next();
            ServerPlayer player = onlinePlayers.get(entry.getKey());

            if (player == null) {
                mapIterator.remove();
                continue;
            }

            Iterator<ScheduledWatcher> watcherIterator = entry.getValue().iterator();
            while (watcherIterator.hasNext()) {
                ScheduledWatcher scheduledWatcher = watcherIterator.next();
                scheduledWatcher.delayTicks--;

                if (scheduledWatcher.delayTicks <= 0) {
                    trySpawnWatcher((ServerLevel) player.level(), player);
                    watcherIterator.remove();
                }
            }

            if (entry.getValue().isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    private static Map<UUID, ServerPlayer> mapPlayers(List<ServerPlayer> players) {
        Map<UUID, ServerPlayer> onlinePlayers = new HashMap<>();
        for (ServerPlayer player : players) {
            onlinePlayers.put(player.getUUID(), player);
        }
        return onlinePlayers;
    }

    private static boolean isPlayerLookingAt(ServerPlayer player, ZeusWatcherEntity zeus) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = zeus.position().add(0.0, zeus.getBbHeight() * 0.65, 0.0);
        Vec3 toWatcher = targetPos.subtract(eyePos);
        double distance = toWatcher.length();

        if (distance > WATCH_DISTANCE) {
            return false;
        }

        Vec3 look = player.getLookAngle().normalize();
        Vec3 direction = toWatcher.normalize();
        return look.dot(direction) > LOOK_DOT_THRESHOLD;
    }

    private static void lookAtPlayer(ZeusWatcherEntity zeus, ServerPlayer player) {
        Vec3 direction = player.position().subtract(zeus.position());
        double yaw = Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0;
        zeus.setYRot((float) yaw);
        zeus.setYHeadRot((float) yaw);
    }

    private static void vanish(ServerLevel level, ServerPlayer player, ZeusWatcherEntity zeus) {
        Vec3 pos = zeus.position();
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 1.0, pos.z, 35, 0.45, 0.7, 0.45, 0.025);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0, pos.z, 25, 0.35, 0.55, 0.35, 0.05);
        level.playSound(null, zeus.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 0.55F);
        player.displayClientMessage(Component.literal(randomVanishMessage(level)), false);
        zeus.discard();
    }

    private static String randomVanishMessage(ServerLevel level) {
        return VANISH_MESSAGES[level.random.nextInt(VANISH_MESSAGES.length)];
    }

    private static void removeWatcher(WatcherData data, boolean discard) {
        if (discard && !data.zeus.isRemoved()) {
            data.zeus.discard();
        }
    }

    private static class WatcherData {
        private final ZeusWatcherEntity zeus;
        private int age;

        private WatcherData(ZeusWatcherEntity zeus) {
            this.zeus = zeus;
            this.age = 0;
        }
    }

    private static class ScheduledMessage {
        private int delayTicks;
        private final Component message;

        private ScheduledMessage(int delayTicks, Component message) {
            this.delayTicks = delayTicks;
            this.message = message;
        }
    }

    private static class ScheduledWatcher {
        private int delayTicks;

        private ScheduledWatcher(int delayTicks) {
            this.delayTicks = delayTicks;
        }
    }
}
