package com.example;

// Importing necessary classes
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class ExampleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerPlayerEvents.AFTER_RESPAWN.register(this::handlePlayerDeath);

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("upgradearmor")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> {
                        if (context.getSource().getEntity() instanceof PlayerEntity) {
                            PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
                            upgradeArmor(player);
                            context.getSource().sendFeedback(() -> Text.of("Armor upgraded to the next tier!"), false);
                        }
                        return 1;
                    }));
        });
    }

    private void handlePlayerDeath(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        DamageSource source = oldPlayer.getRecentDamageSource();
        if (source != null && source.getAttacker() instanceof PlayerEntity) {
            PlayerEntity killer = (PlayerEntity) source.getAttacker();
            upgradeArmor(killer);
        }

        // Clear only the armor slots of the old player
        for (int i = 0; i < oldPlayer.getInventory().armor.size(); i++) {
            oldPlayer.getInventory().armor.get(i).setCount(0);
        }
    }

    private void upgradeArmor(PlayerEntity player) {
        // Define the upgrade path for each armor type
        Map<Item, Item> upgradePath = new HashMap<>();
        upgradePath.put(Items.LEATHER_BOOTS, Items.IRON_BOOTS);
        upgradePath.put(Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS);
        upgradePath.put(Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE);
        upgradePath.put(Items.LEATHER_HELMET, Items.IRON_HELMET);

        upgradePath.put(Items.IRON_BOOTS, Items.DIAMOND_BOOTS);
        upgradePath.put(Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS);
        upgradePath.put(Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE);
        upgradePath.put(Items.IRON_HELMET, Items.DIAMOND_HELMET);

        upgradePath.put(Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
        upgradePath.put(Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        upgradePath.put(Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        upgradePath.put(Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);

        // Upgrade each armor piece based on the defined path
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack currentArmorPiece = player.getInventory().armor.get(i);
            if (!(currentArmorPiece.getItem() instanceof ArmorItem)) continue;

            Item upgradedArmorItem = upgradePath.get(currentArmorPiece.getItem());
            if (upgradedArmorItem != null) {
                ItemStack upgradedArmorPiece = new ItemStack(upgradedArmorItem);
                upgradedArmorPiece.setDamage(currentArmorPiece.getDamage());

                if (currentArmorPiece.hasNbt()) {
                    upgradedArmorPiece.setNbt(currentArmorPiece.getNbt().copy());
                }

                player.getInventory().armor.set(i, upgradedArmorPiece);
            }
        }
    }
}
