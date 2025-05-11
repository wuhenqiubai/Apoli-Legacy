package io.github.apace100.apoli.power.factory.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.item.HolderAction;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.ITEM_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
            Tuple::getB));
        register(ChoiceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
            Tuple::getB));
        register(DelayAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.ITEM_ACTION, worldAndStack -> !worldAndStack.getA().isClientSide()));

        register(new ActionFactory<>(Apoli.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
            (data, worldAndStack) -> {
                worldAndStack.getB().shrink(data.getInt("amount"));
            }));
        register(new ActionFactory<>(Apoli.identifier("modify"), new SerializableData()
            .add("modifier", SerializableDataTypes.IDENTIFIER),
            (data, worldAndStack) -> {
                MinecraftServer server = worldAndStack.getA().getServer();
                if(server != null) {
                    ResourceLocation id = data.getId("modifier");
                    LootDataManager lootManager = server.getLootData();
                    LootItemFunction lootFunction = lootManager.getElement(LootDataType.MODIFIER, id);
                    if (lootFunction == null) {
                        Apoli.LOGGER.info("Unknown item modifier used in `modify` action: " + id);
                        return;
                    }
                    ServerLevel serverWorld = server.overworld();
                    ItemStack stack = worldAndStack.getB();
                    LootParams lootContextParameterSet = new LootParams.Builder(serverWorld).withParameter(LootContextParams.ORIGIN, new Vec3(0, 0,0)).create(LootContextParamSets.COMMAND);
                    LootContext lootContext = new LootContext.Builder(lootContextParameterSet).create(null);
                    ItemStack newStack = lootFunction.apply(stack, lootContext);
                    ((MutableItemStack)stack).setFrom(newStack);
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("damage"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1)
            .add("ignore_unbreaking", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> {
                if (worldAndStack.getB().isDamageableItem()) {
                    int amount = data.getInt("amount");
                    int i;
                    if (amount > 0 && !data.getBoolean("ignore_unbreaking")) {
                        i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, worldAndStack.getB());
                        int j = 0;

                        for(int k = 0; i > 0 && k < amount; ++k) {
                            if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(worldAndStack.getB(), i, worldAndStack.getA().random)) {
                                ++j;
                            }
                        }

                        amount -= j;
                        if (amount <= 0) {
                            return;
                        }
                    }

                    i = worldAndStack.getB().getDamageValue() + amount;
                    worldAndStack.getB().setDamageValue(i);
                    if(i >= worldAndStack.getB().getMaxDamage()) {
                        worldAndStack.getB().shrink(1);
                        worldAndStack.getB().setDamageValue(0);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("merge_nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.STRING),
            (data, worldAndStack) -> {
                String nbtString = data.get("nbt");
                try {
                    CompoundTag nbt = new TagParser(new StringReader(nbtString)).readStruct();
                    worldAndStack.getB().getOrCreateTag().merge(nbt);
                } catch (CommandSyntaxException e) {
                    Apoli.LOGGER.error("Failed `merge_nbt` item action due to malformed nbt string: \"" + nbtString + "\"");
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("remove_enchantment"), new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
            .add("enchantments", SerializableDataType.list(SerializableDataTypes.ENCHANTMENT), null)
            .add("levels", SerializableDataTypes.INT, null)
            .add("reset_repair_cost", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> {
                ItemStack stack = worldAndStack.getB();
                if(!stack.hasTag()) {
                    return;
                }
                List<Enchantment> enchs = new LinkedList<>();
                data.<Enchantment>ifPresent("enchantment", enchs::add);
                data.<List<Enchantment>>ifPresent("enchantments", enchs::addAll);
                int levels = -1;
                if(data.isPresent("levels")) {
                    levels = data.getInt("levels");
                }
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                if(enchs.size() > 0) {
                    for(Enchantment ench : enchs) {
                        if(enchants.containsKey(ench)) {
                            int newLevel = levels == -1 ? 0 : enchants.get(ench) - data.getInt("levels");
                            if(newLevel <= 0) {
                                enchants.remove(ench);
                            } else {
                                enchants.put(ench, newLevel);
                            }
                        }
                    }
                } else {
                    Map<Enchantment, Integer> newEnchants = new LinkedHashMap<>();
                    for(Enchantment e : enchants.keySet()) {
                        int newLevel = levels == -1 ? 0 : enchants.get(e) - data.getInt("levels");
                        if(newLevel > 0) {
                            newEnchants.put(e, newLevel);
                        }
                    }
                    enchants = newEnchants;
                }
                EnchantmentHelper.setEnchantments(enchants, stack);
                if(data.getBoolean("reset_repair_cost") && !stack.isEnchanted()) {
                    stack.setRepairCost(0);
                }
            }));
        register(HolderAction.getFactory());
    }

    private static void register(ActionFactory<Tuple<Level, ItemStack>> actionFactory) {
        Registry.register(ApoliRegistries.ITEM_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
