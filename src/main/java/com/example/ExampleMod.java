package com.example;

// Importing necessary classes from the Fabric API and Minecraft
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ExampleMod implements ModInitializer {

	// This method is called when the mod is initialized
	@Override
	public void onInitialize() {
		// Register an event listener for the AFTER_RESPAWN event
		ServerPlayerEvents.AFTER_RESPAWN.register(this::handlePlayerDeath);

		// Register a new command "/upgradearmor" for players with permission level 2
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("upgradearmor")
					.requires(source -> source.hasPermissionLevel(2)) // Set permission level
					.executes(context -> {
						// Check if the entity executing the command is a player
						if (context.getSource().getEntity() instanceof PlayerEntity) {
							PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
							upgradeArmor(player); // Upgrade the player's armor
							// Send feedback to the player who executed the command
							context.getSource().sendFeedback(() -> Text.of("Armor upgraded to Netherite!"), false);
						}
						return 1; // Indicate successful command execution
					}));
		});
	}

	// Method to handle logic when a player dies and respawns
	private void handlePlayerDeath(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
		// Get the damage source of the player's death
		DamageSource source = oldPlayer.getRecentDamageSource();
		if (source != null && source.getAttacker() instanceof PlayerEntity) {
			// If the killer is a player, upgrade their armor
			PlayerEntity killer = (PlayerEntity) source.getAttacker();
			upgradeArmor(killer);
		}
		// Clear the old player's armor inventory to prevent dropping
		oldPlayer.getInventory().armor.clear();
	}

	// Method to upgrade a player's armor to Netherite
	private void upgradeArmor(PlayerEntity player) {
		// Iterate through each item in the player's armor inventory
		player.getInventory().armor.forEach(itemStack -> {
			// Skip non-armor items and non-diamond armor
			if (!(itemStack.getItem() instanceof ArmorItem)) return;
			ArmorItem armorItem = (ArmorItem) itemStack.getItem();
			if (armorItem.getMaterial() != ArmorMaterials.DIAMOND) return;

			// Create a new ItemStack for the Netherite armor counterpart
			ItemStack netheriteArmor = new ItemStack(getNetheriteCounterpart(itemStack.getItem()));
			netheriteArmor.setDamage(itemStack.getDamage()); // Set damage value to new armor

			// Copy NBT data (including enchantments) to the new armor
			if (itemStack.hasNbt()) {
				netheriteArmor.setNbt(itemStack.getNbt().copy());
			}

			// Remove the old armor item and replace it with the new Netherite armor
			itemStack.setCount(0);
			player.getInventory().armor.set(player.getInventory().armor.indexOf(itemStack), netheriteArmor);
		});
	}

	// Method to get the Netherite counterpart of a diamond armor item
	private Item getNetheriteCounterpart(Item item) {
		// Return the corresponding Netherite item for each diamond armor piece
		if (item == Items.DIAMOND_HELMET) {
			return Items.NETHERITE_HELMET;
		} else if (item == Items.DIAMOND_CHESTPLATE) {
			return Items.NETHERITE_CHESTPLATE;
		} else if (item == Items.DIAMOND_LEGGINGS) {
			return Items.NETHERITE_LEGGINGS;
		} else if (item == Items.DIAMOND_BOOTS) {
			return Items.NETHERITE_BOOTS;
		} else {
			// Return the original item if it's not a diamond armor piece
			return item;
		}
	}
}
