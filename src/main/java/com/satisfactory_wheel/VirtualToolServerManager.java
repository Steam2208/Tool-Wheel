package com.satisfactory_wheel;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualToolServerManager {
    public static class StoredState {
        public final int slot;
        public final ItemStack originalItem;
        public String activeToolId;

        public StoredState(int slot, ItemStack originalItem, String activeToolId) {
            this.slot = slot;
            this.originalItem = originalItem != null ? originalItem : ItemStack.EMPTY;
            this.activeToolId = activeToolId;
        }
    }

    private static final Map<UUID, StoredState> ACTIVE_TOOLS = new ConcurrentHashMap<>();
    private static final String NBT_KEY = "satisfactory_wheel_state";

    private static CompoundTag getPlayerData(ServerPlayer player) {
        return ((IEntityExtension) (Object) player).getPersistentData();
    }

    public static boolean isVirtualStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains("SatisfactoryVirtualTool");
    }

    public static ItemStack createVirtualStack(Item item) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("SatisfactoryVirtualTool", true);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return stack;
    }

    public static void handleEquip(ServerPlayer player, String toolId) {
        if (toolId == null || toolId.isEmpty() || toolId.equalsIgnoreCase("unequip")) {
            unequip(player);
            return;
        }

        ResourceLocation resLoc = ResourceLocation.tryParse(toolId);
        if (resLoc == null || !BuiltInRegistries.ITEM.containsKey(resLoc)) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(resLoc);
        if (item == Items.AIR) {
            return;
        }

        StoredState state = ACTIVE_TOOLS.get(player.getUUID());
        if (state != null && toolId.equals(state.activeToolId)) {
            // Toggled the same tool -> unequip!
            unequip(player);
            return;
        }

        if (state != null) {
            // Already holding a virtual tool: swap to another virtual tool, keeping originalItem!
            state.activeToolId = toolId;
            ItemStack virtualStack = createVirtualStack(item);
            player.getInventory().setItem(state.slot, virtualStack);
            savePersistentState(player, state);
            player.containerMenu.broadcastChanges();
            player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 0.5f, 1.4f);
            return;
        }

        // New virtual tool
        int currentSlot = player.getInventory().selected;
        ItemStack currentItem = player.getMainHandItem().copy();
        state = new StoredState(currentSlot, currentItem, toolId);
        ACTIVE_TOOLS.put(player.getUUID(), state);
        savePersistentState(player, state);

        ItemStack virtualStack = createVirtualStack(item);
        player.getInventory().setItem(currentSlot, virtualStack);
        player.containerMenu.broadcastChanges();
        player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 0.5f, 1.4f);
    }

    public static void unequip(ServerPlayer player) {
        StoredState state = ACTIVE_TOOLS.remove(player.getUUID());
        clearPersistentState(player);

        if (state != null) {
            player.getInventory().setItem(state.slot, state.originalItem);
            player.containerMenu.broadcastChanges();
        } else {
            // Fallback: check if main hand holds a virtual tool
            ItemStack hand = player.getMainHandItem();
            if (isVirtualStack(hand)) {
                player.getInventory().setItem(player.getInventory().selected, ItemStack.EMPTY);
                player.containerMenu.broadcastChanges();
            }
        }
    }

    private static void savePersistentState(ServerPlayer player, StoredState state) {
        try {
            CompoundTag root = getPlayerData(player);
            CompoundTag tag = new CompoundTag();
            tag.putInt("slot", state.slot);
            tag.putString("tool_id", state.activeToolId);
            if (!state.originalItem.isEmpty()) {
                tag.put("original_item", state.originalItem.saveOptional(player.registryAccess()));
            }
            root.put(NBT_KEY, tag);
        } catch (Exception ignored) {}
    }

    private static void clearPersistentState(ServerPlayer player) {
        try {
            getPlayerData(player).remove(NBT_KEY);
        } catch (Exception ignored) {}
    }

    private static void restoreFromPersistent(ServerPlayer player) {
        try {
            CompoundTag root = getPlayerData(player);
            if (root.contains(NBT_KEY)) {
                CompoundTag tag = root.getCompound(NBT_KEY);
                int slot = tag.getInt("slot");
                ItemStack original = ItemStack.EMPTY;
                if (tag.contains("original_item")) {
                    CompoundTag itemTag = tag.getCompound("original_item");
                    original = ItemStack.parseOptional(player.registryAccess(), itemTag);
                }
                player.getInventory().setItem(slot, original);
                clearPersistentState(player);
                player.containerMenu.broadcastChanges();
            }
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StoredState state = ACTIVE_TOOLS.get(player.getUUID());
            if (state != null) {
                // If hotbar slot changed -> unequip
                if (player.getInventory().selected != state.slot) {
                    unequip(player);
                    return;
                }
                // If the item in slot was changed or consumed
                ItemStack inSlot = player.getInventory().getItem(state.slot);
                if (!isVirtualStack(inSlot)) {
                    unequip(player);
                    return;
                }
            }

            // Anti-dupe check: remove any virtual items that ended up in other slots
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (state != null && i == state.slot) continue;
                ItemStack stack = player.getInventory().getItem(i);
                if (isVirtualStack(stack)) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (ACTIVE_TOOLS.containsKey(player.getUUID())) {
                unequip(player);
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (isVirtualStack(event.getEntity().getItem())) {
            event.setCanceled(true);
            event.getEntity().discard();
            if (event.getPlayer() instanceof ServerPlayer player) {
                unequip(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            unequip(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            unequip(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restoreFromPersistent(player);
        }
    }
}
