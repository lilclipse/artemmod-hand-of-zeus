package com.artem.artemmod.event;

import com.artem.artemmod.item.ModItems;
import com.artem.artemmod.item.ZeusHandItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ZeusKarmaEvents {
    private static final Set<UUID> STARTER_ITEMS_GIVEN = new HashSet<>();
    private static int worldEventTicker = 0;

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayer player && !STARTER_ITEMS_GIVEN.contains(player.getUUID())) {
                giveStarterItems(player);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(damageSource.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            if (isGoodMob(entity)) {
                ZeusHandItem.addKarma(player, 3);
                player.displayClientMessage(Component.literal("§8Карма потемнела."), false);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            worldEventTicker++;
            if (worldEventTicker < 20 * 25) {
                return;
            }
            worldEventTicker = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                rollKarmaWorldEvent(player);
            }
        });
    }

    private static void rollKarmaWorldEvent(ServerPlayer player) {
        int karma = ZeusHandItem.getKarma(player);
        if (karma < 10) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        float chance = Math.min(0.04F + (karma * 0.002F), 0.18F);
        if (level.random.nextFloat() >= chance) {
            return;
        }

        if (karma >= 30 && level.random.nextFloat() < 0.45F) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 6, 0));
            player.displayClientMessage(Component.literal("§8Ты больше не зовешь его. Он приходит сам."), false);
            level.playSound(null, player.blockPosition(), SoundEvents.WITCH_AMBIENT, SoundSource.HOSTILE, 0.75F, 0.5F);
            ZeusWatcherManager.trySpawnWatcher(level, player);
        } else if (karma >= 20 && level.random.nextFloat() < 0.50F) {
            player.displayClientMessage(Component.literal("§8Где-то рядом стало тише."), false);
            level.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.HOSTILE, 0.65F, 0.55F);
        } else {
            player.displayClientMessage(Component.literal("§8Небо помнит."), false);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.45F, 0.65F);
        }
    }

    private static void giveStarterItems(ServerPlayer player) {
        STARTER_ITEMS_GIVEN.add(player.getUUID());

        giveOrDrop(player, new ItemStack(ModItems.ZEUS_HAND));
        giveOrDrop(player, new ItemStack(ModItems.ZEUS_GUIDE_BOOK));
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static boolean isGoodMob(LivingEntity entity) {
        return entity instanceof Animal
                || entity instanceof AbstractVillager
                || entity instanceof IronGolem
                || entity instanceof SnowGolem;
    }
}
