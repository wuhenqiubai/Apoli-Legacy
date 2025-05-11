package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class StackPowerUtil {

    public static void addPower(ItemStack stack, EquipmentSlot slot, ResourceLocation powerId) {
        addPower(stack, slot, powerId, false, false);
    }

    public static void addPower(ItemStack stack, EquipmentSlot slot, ResourceLocation powerId, boolean isHidden, boolean isNegative) {
        StackPower stackPower = new StackPower();
        stackPower.slot = slot;
        stackPower.powerId = powerId;
        stackPower.isHidden = isHidden;
        stackPower.isNegative = isNegative;
        addPower(stack, stackPower);
    }

    public static void addPower(ItemStack stack, StackPower stackPower) {
        CompoundTag nbt = stack.getOrCreateTag();
        ListTag list;
        if(nbt.contains("Powers")) {
            Tag elem = nbt.get("Powers");
            if(elem.getId() != NbtType.LIST) {
                Apoli.LOGGER.warn("Can't add power " + stackPower.powerId + " to item stack "
                    + stack + ", as it contains conflicting NBT data.");
                return;
            }
            list = (ListTag)elem;
        } else {
            list = new ListTag();
            nbt.put("Powers", list);
        }
        list.add(stackPower.toNbt());
    }

    public static void removePower(ItemStack stack, EquipmentSlot slot, ResourceLocation powerId) {
        CompoundTag nbt = stack.getOrCreateTag();
        ListTag list;
        if(nbt.contains("Powers")) {
            Tag elem = nbt.get("Powers");
            if(elem.getId() != NbtType.LIST) {
                Apoli.LOGGER.warn("Can't remove power " + powerId + " from item stack "
                    + stack + ", as it contains conflicting NBT data.");
                return;
            }
            list = (ListTag)elem;
            int found = -1;
            while(list.size() > 0) {
                for(int i = 0; i < list.size(); i++) {
                    StackPower sp = StackPower.fromNbt(list.getCompound(i));
                    if(sp.powerId.equals(powerId) && sp.slot == slot) {
                        found = i;
                        break;
                    }
                }
                if(found >= 0) {
                    list.remove(found);
                    found = -1;
                } else {
                    break;
                }
            }
        }
    }

    public static List<StackPower> getPowers(ItemStack stack, EquipmentSlot slot) {
        CompoundTag nbt = stack.getTag();
        ListTag list;
        List<StackPower> powers = new LinkedList<>();
        if(stack.getItem() instanceof PowerGrantingItem pgi) {
            powers.addAll(pgi.getPowers(stack, slot));
        }
        if(nbt != null && nbt.contains("Powers")) {
            Tag elem = nbt.get("Powers");
            if(elem.getId() != NbtType.LIST) {
                return List.of();
            }
            list = (ListTag)elem;
            list.stream().map(p -> {
                if(p.getId() == NbtType.COMPOUND) {
                    return StackPower.fromNbt((CompoundTag)p);
                } else {
                    Apoli.LOGGER.warn("Invalid power format on stack nbt, stack = " + stack + ", nbt = " + p);
                }
                return null;
            }).filter(sp -> sp != null && sp.slot == slot).forEach(powers::add);
        }
        return powers;
    }

    public static class StackPower {
        public EquipmentSlot slot;
        public ResourceLocation powerId;
        public boolean isHidden;
        public boolean isNegative;

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("Slot", slot.getName());
            nbt.putString("Power", powerId.toString());
            nbt.putBoolean("Hidden", isHidden);
            nbt.putBoolean("Negative", isNegative);
            return nbt;
        }

        public static StackPower fromNbt(CompoundTag nbt) {
            StackPower stackPower = new StackPower();
            stackPower.slot = EquipmentSlot.byName(nbt.getString("Slot"));
            stackPower.powerId = new ResourceLocation(nbt.getString("Power"));
            stackPower.isHidden = nbt.contains("Hidden") && nbt.getBoolean("Hidden");
            stackPower.isNegative = nbt.contains("Negative") && nbt.getBoolean("Negative");
            return stackPower;
        }
    }
}
