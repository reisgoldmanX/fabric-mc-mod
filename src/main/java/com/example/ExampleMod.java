package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import java.util.HashMap;
import java.util.Map;

public class ExampleMod implements ModInitializer {

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("upgradearmor")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						if (context.getSource().getEntity() instanceof ServerPlayerEntity) {
							ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
							upgradeArmor(player);
							spawnUpgradeParticles(player);
							context.getSource().sendFeedback(new LiteralText("Armor upgraded to the next tier!"), false);
						}

						return 1;
					}));
			dispatcher.register(CommandManager.literal("downgradearmor")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						if (context.getSource().getEntity() instanceof ServerPlayerEntity) {
							ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
							downgradeArmor(player);
							spawnDowngradeParticles(player);
							context.getSource().sendFeedback(new LiteralText("Armor downgraded to the previous tier!"), false);
						}
						return 1;
					}));
			dispatcher.register(CommandManager.literal("enchantarmor")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> {
						if (context.getSource().getEntity() instanceof ServerPlayerEntity) {
							ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
							upgradeEnchantments(player);
							context.getSource().sendFeedback(new LiteralText(Formatting.GREEN + "Armor enchantments upgraded successfully!"), false);
							return 1;
						} else {
							context.getSource().sendError(new LiteralText(Formatting.RED + "Only players can execute this command!"));
							return 0;
						}
					}));
		});
	}

	private void spawnUpgradeParticles(ServerPlayerEntity player) {
		ServerWorld serverWorld = player.getServerWorld();
		if (serverWorld != null) {
			ParticleEffect particleEffect = ParticleTypes.TOTEM_OF_UNDYING;
			serverWorld.spawnParticles(particleEffect, player.getX(), player.getY() + player.getHeight() / 2.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnDowngradeParticles(ServerPlayerEntity player) {
		ServerWorld serverWorld = player.getServerWorld();
		if (serverWorld != null) {
			ParticleEffect particleEffect = ParticleTypes.DRIPPING_LAVA;
			serverWorld.spawnParticles(particleEffect, player.getX(), player.getY() + player.getHeight() / 2.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void downgradeArmor(ServerPlayerEntity player) {
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
		for (ItemStack currentArmorPiece : player.getInventory().armor) {
			if (!(currentArmorPiece.getItem() instanceof ArmorItem)) continue;
			Item downgradedArmorItem = downgradePath.get(currentArmorPiece.getItem());
			if (downgradedArmorItem != null) {
				ItemStack downgradedArmorPiece = new ItemStack(downgradedArmorItem);
				downgradedArmorPiece.setDamage(currentArmorPiece.getDamage());
				if (currentArmorPiece.hasNbt()) {
					downgradedArmorPiece.setNbt(currentArmorPiece.getNbt().copy());
				}
				player.equipStack(EquipmentSlot.CHEST, downgradedArmorPiece);
			}
		}
	}

	private void upgradeArmor(ServerPlayerEntity player) {
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
		for (ItemStack currentArmorPiece : player.getInventory().armor) {
			if (!(currentArmorPiece.getItem() instanceof ArmorItem)) continue;
			Item upgradedArmorItem = upgradePath.get(currentArmorPiece.getItem());
			if (upgradedArmorItem != null) {
				ItemStack upgradedArmorPiece = new ItemStack(upgradedArmorItem);
				upgradedArmorPiece.setDamage(currentArmorPiece.getDamage());
				if (currentArmorPiece.hasNbt()) {
					upgradedArmorPiece.setNbt(currentArmorPiece.getNbt().copy());
				}
				player.equipStack(EquipmentSlot.CHEST, upgradedArmorPiece);
			}
		}
	}

	private void upgradeEnchantments(ServerPlayerEntity player) {
		Map<Item, Enchantment> enchantmentsToAdd = new HashMap<>();
		enchantmentsToAdd.put(Items.LEATHER_BOOTS, Enchantments.PROTECTION);
		enchantmentsToAdd.put(Items.LEATHER_LEGGINGS, Enchantments.FIRE_PROTECTION);
		enchantmentsToAdd.put(Items.LEATHER_CHESTPLATE, Enchantments.BLAST_PROTECTION);
		enchantmentsToAdd.put(Items.LEATHER_HELMET, Enchantments.PROJECTILE_PROTECTION);
		for (ItemStack armorPiece : player.getInventory().armor) {
			if (armorPiece.getItem() instanceof ArmorItem) {
				ArmorItem armorItem = (ArmorItem) armorPiece.getItem();
				Enchantment enchantment = enchantmentsToAdd.get(armorItem);
				if (enchantment != null) {
					armorPiece.addEnchantment(enchantment, 1);
				}
			}
		}
	}
}
