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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class ExampleMod implements ModInitializer {

	@Override
	public void onInitialize() {
		ServerPlayerEvents.AFTER_RESPAWN.register(this::handlePlayerRespawn);

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("upgradearmor")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						if (context.getSource().getEntity() instanceof PlayerEntity) {
							PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
							upgradeArmor(player);
							spawnUpgradeParticles(player);
							context.getSource().sendFeedback(() -> Text.of("Armor upgraded to the next tier!"), false);
						}

						return 1;
					}));
			// Register downgradearmor command
			dispatcher.register(CommandManager.literal("downgradearmor")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						if (context.getSource().getEntity() instanceof PlayerEntity) {
							PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
							downgradeArmor(player);
							spawnDowngradeParticles(player);
							context.getSource().sendFeedback(() -> Text.of("Armor downgraded to the previous tier!"), false);
						}
						return 1;
					}));
			// Register enchantarmor command
			dispatcher.register(CommandManager.literal("enchantarmor")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						if (context.getSource().getEntity() instanceof PlayerEntity) {
							PlayerEntity player = (PlayerEntity) context.getSource().getEntity();

							context.getSource().sendFeedback(() -> Text.of("Armor enchanted successfully!"), false);
						}
						return 1;
					}));
		});
	}
	private void spawnUpgradeParticles(PlayerEntity player) {
		if (player.getWorld() instanceof ServerWorld) { // Use getWorld() instead of direct access
			ServerWorld serverWorld = (ServerWorld) player.getWorld(); // Cast to ServerWorld
			ParticleEffect particleEffect = ParticleTypes.TOTEM_OF_UNDYING; // Use Totem of Undying particle

			// Define the area around the player to spawn particles
			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();

			// Totem of Undying particles are typically concentrated and don't require a spread like happy villager particles
			serverWorld.spawnParticles(particleEffect, x, y + player.getHeight() / 2.0, z, 30, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnDowngradeParticles(PlayerEntity player) {
		if (player.getWorld() instanceof ServerWorld) { // Use getWorld() instead of direct access
			ServerWorld serverWorld = (ServerWorld) player.getWorld(); // Cast to ServerWorld
			ParticleEffect particleEffect = ParticleTypes.DRIPPING_LAVA; // Use Totem of Undying particle

			// Define the area around the player to spawn particles
			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();

			// Totem of Undying particles are typically concentrated and don't require a spread like happy villager particles
			serverWorld.spawnParticles(particleEffect, x, y + player.getHeight() / 2.0, z, 30, 0.5, 0.5, 0.5, 0.1);
		}
	}


	private void handlePlayerRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
		// Downgrade armor for the respawned player
		downgradeArmor(newPlayer);

		DamageSource source = oldPlayer.getRecentDamageSource();
		if (source != null && source.getAttacker() instanceof PlayerEntity) {
			PlayerEntity killer = (PlayerEntity) source.getAttacker();
			// Upgrade killer's armor
			upgradeArmor(killer);
		}
	}
	private void downgradeArmor(PlayerEntity player) {
		// Define the downgrade path
		Map<Item, Item> downgradePath = new HashMap<>();
		downgradePath.put(Items.NETHERITE_BOOTS, Items.DIAMOND_BOOTS);
		downgradePath.put(Items.NETHERITE_LEGGINGS, Items.DIAMOND_LEGGINGS);
		downgradePath.put(Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE);
		downgradePath.put(Items.NETHERITE_HELMET, Items.DIAMOND_HELMET);

		downgradePath.put(Items.DIAMOND_BOOTS, Items.IRON_BOOTS);
		downgradePath.put(Items.DIAMOND_LEGGINGS, Items.IRON_LEGGINGS);
		downgradePath.put(Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE);
		downgradePath.put(Items.DIAMOND_HELMET, Items.IRON_HELMET);

		downgradePath.put(Items.IRON_BOOTS, Items.LEATHER_BOOTS);
		downgradePath.put(Items.IRON_LEGGINGS, Items.LEATHER_LEGGINGS);
		downgradePath.put(Items.IRON_CHESTPLATE, Items.LEATHER_CHESTPLATE);
		downgradePath.put(Items.IRON_HELMET, Items.LEATHER_HELMET);

		// Downgrade each armor piece based on the defined path
		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			ItemStack currentArmorPiece = player.getInventory().armor.get(i);
			if (!(currentArmorPiece.getItem() instanceof ArmorItem)) continue;

			Item downgradedArmorItem = downgradePath.get(currentArmorPiece.getItem());
			if (downgradedArmorItem != null) {
				ItemStack downgradedArmorPiece = new ItemStack(downgradedArmorItem);
				downgradedArmorPiece.setDamage(currentArmorPiece.getDamage());

				if (currentArmorPiece.hasNbt()) {
					downgradedArmorPiece.setNbt(currentArmorPiece.getNbt().copy());
				}

				player.getInventory().armor.set(i, downgradedArmorPiece);
			}
		}
	}

	private void upgradeArmor(PlayerEntity player) {
		// Define the enchantments to add for each armor piece
		Map<Item, Enchantment> enchantmentsToAdd = new HashMap<>();
		enchantmentsToAdd.put(Items.LEATHER_BOOTS, Enchantments.PROTECTION);
		enchantmentsToAdd.put(Items.LEATHER_LEGGINGS, Enchantments.FIRE_PROTECTION);
		enchantmentsToAdd.put(Items.LEATHER_CHESTPLATE, Enchantments.BLAST_PROTECTION);
		enchantmentsToAdd.put(Items.LEATHER_HELMET, Enchantments.PROJECTILE_PROTECTION);

		// Iterate over the player's armor pieces
		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			ItemStack currentArmorPiece = player.getInventory().armor.get(i);
			if (!(currentArmorPiece.getItem() instanceof ArmorItem)) continue;

			// Check if there are enchantments to add for this armor piece
			Enchantment enchantment = enchantmentsToAdd.get(currentArmorPiece.getItem());
			if (enchantment != null) {
				// Add the specified enchantment to the armor piece
				currentArmorPiece.addEnchantment(enchantment, 1); // You can change the enchantment level here
			}
		}

		// Send feedback to the player
		player.sendMessage(Text.of("Armor enchanted successfully!"), false);

		boolean hasNoArmor = player.getInventory().armor.stream().allMatch(itemStack -> itemStack.isEmpty());

		if (hasNoArmor) {
			// Equip player with leather armor set if they have no armor
			player.getInventory().armor.set(0, new ItemStack(Items.LEATHER_BOOTS));
			player.getInventory().armor.set(1, new ItemStack(Items.LEATHER_LEGGINGS));
			player.getInventory().armor.set(2, new ItemStack(Items.LEATHER_CHESTPLATE));
			player.getInventory().armor.set(3, new ItemStack(Items.LEATHER_HELMET));
			return; // Stop further execution to avoid overriding this set with higher tier armor
		}
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
