package com.artem.artemmod.event;

import com.artem.artemmod.item.ModItems;
import com.artem.artemmod.item.ZeusHandItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ZeusKarmaEvents {
    private static final Set<UUID> STARTER_ITEMS_GIVEN = new HashSet<>();

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
