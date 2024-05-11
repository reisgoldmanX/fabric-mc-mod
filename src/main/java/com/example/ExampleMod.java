
// Deenchant armor should be fixed



package projectexecute.execute;

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
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ProjectExecute implements ModInitializer {
	private final static int MAX_ENCHANTMENT_LEVEL = 5;
	public static final String MOD_ID = "execute";
	public static final Logger LOGGER = LoggerFactory.getLogger("MOD_ID");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

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
							enchantArmor(player);

							context.getSource().sendFeedback(() -> Text.of("Armor enchanted successfully!"), false);
						}
						return 1;
					}));
		});
	}
	private void enchantArmor(PlayerEntity player) {
		Item lastArmorType = null; // Track the last armor type processed

		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack armorPiece = player.getInventory().getStack(i);
			if (!(armorPiece.getItem() instanceof ArmorItem)) continue;

			// Reset enchantment level incrementing if the armor type changes
			if (lastArmorType != armorPiece.getItem()) {
				lastArmorType = armorPiece.getItem();
			}

			// Get current Protection enchantment level
			int currentLevel = EnchantmentHelper.getLevel(Enchantments.PROTECTION, armorPiece);

			// Increment the enchantment level by 1, up to a max of 5
			int newLevel = Math.min(5, currentLevel + 1);

			// If there is room to upgrade the enchantment
			if (newLevel > currentLevel) {
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(armorPiece);
				enchantments.put(Enchantments.PROTECTION, newLevel);
				EnchantmentHelper.set(enchantments, armorPiece);
			}
		}
		spawnUpgradeParticles(player);
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

		// Upgrade armor for the killer, if applicable
		DamageSource source = oldPlayer.getRecentDamageSource();
		if (source != null && source.getAttacker() instanceof PlayerEntity) {
			PlayerEntity killer = (PlayerEntity) source.getAttacker();
			upgradeArmor(killer);
			// Synchronize the killer's inventory changes
			killer.playerScreenHandler.sendContentUpdates();
		}

		// Synchronize the respawned player's inventory changes
		newPlayer.playerScreenHandler.sendContentUpdates();
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

		// Process each armor piece
		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			ItemStack armorPiece = player.getInventory().armor.get(i);
			if (!(armorPiece.getItem() instanceof ArmorItem)) continue;

			Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(armorPiece);
			if (!enchantments.isEmpty()) {
				// Reduce enchantment level step by step
				boolean hasEnchantmentsLeft = false;
				for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
					int newLevel = entry.getValue() - 1;
					if (newLevel > 0) {
						enchantments.put(entry.getKey(), newLevel);
						hasEnchantmentsLeft = true;
					} else {
						enchantments.remove(entry.getKey());
					}
				}
				EnchantmentHelper.set(enchantments, armorPiece);
				if (hasEnchantmentsLeft) {
					continue; // Continue to next iteration to handle other items without changing material yet
				}
			}

			// Downgrade material once no enchantments are left
			Item downgradedArmorItem = downgradePath.get(armorPiece.getItem());
			if (downgradedArmorItem != null) {
				ItemStack downgradedArmorPiece = new ItemStack(downgradedArmorItem);
				downgradedArmorPiece.setDamage(armorPiece.getDamage());

				// Reapply a full set of enchantments to the downgraded armor
				enchantments.clear();
				enchantments.put(Enchantments.PROTECTION, 5);
				EnchantmentHelper.set(enchantments, downgradedArmorPiece);

				// If the original armor had NBT data, copy it, excluding durability and enchantments
				if (armorPiece.hasNbt()) {
					NbtCompound nbt = armorPiece.getNbt().copy();
					nbt.remove("Damage");
					nbt.remove("Enchantments");
					downgradedArmorPiece.setNbt(nbt);
				}

				player.getInventory().armor.set(i, downgradedArmorPiece);
			}
		}
	}

	private void upgradeArmor(PlayerEntity player) {


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
		boolean allArmorFullyEnchanted = true;
		for (ItemStack armorPiece : player.getInventory().armor) {
			if (!(armorPiece.getItem() instanceof ArmorItem)) {
				continue;
			}
			int currentEnchantmentLevel = EnchantmentHelper.getLevel(Enchantments.PROTECTION, armorPiece);
			if (currentEnchantmentLevel < MAX_ENCHANTMENT_LEVEL) {
				allArmorFullyEnchanted = false;
				break;
			}
		}

		if (!allArmorFullyEnchanted) {
			enchantArmor(player);
			return; // Stop execution to allow enchanting to complete before upgrading
		}

		// Only upgrade armor if all pieces are fully enchanted
		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			ItemStack armorPiece = player.getInventory().armor.get(i);
			if (!(armorPiece.getItem() instanceof ArmorItem)) {
				continue;
			}

			Item upgradedArmorItem = upgradePath.get(armorPiece.getItem());
			if (upgradedArmorItem != null) {
				ItemStack upgradedArmorPiece = new ItemStack(upgradedArmorItem);
				upgradedArmorPiece.setDamage(armorPiece.getDamage());
				NbtCompound nbt = armorPiece.getOrCreateNbt().copy();
				nbt.remove("Damage");
				nbt.remove("Enchantments"); // Clear enchantments for new armor
				upgradedArmorPiece.setNbt(nbt);
				player.getInventory().armor.set(i, upgradedArmorPiece);
			}
		}
	}
}
