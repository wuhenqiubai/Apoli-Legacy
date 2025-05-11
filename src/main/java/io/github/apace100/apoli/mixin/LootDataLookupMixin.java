package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.power.ReplaceLootTablePower;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LootDataResolver.class)
public interface LootDataLookupMixin extends LootDataResolver
{
    @Inject(method = "getLootTable", at = @At("HEAD"), cancellable = true)
    private void setTableId(ResourceLocation id, CallbackInfoReturnable<LootTable> cir) {
        if(id.equals(ReplaceLootTablePower.REPLACED_TABLE_UTIL_ID)) {
            LootTable replace = ReplaceLootTablePower.peek();
            Apoli.LOGGER.info("Replacing " + id + " with " + ((IdentifiedLootTable)replace).getId());
            cir.setReturnValue(replace);
            //cir.setReturnValue(getTable(ReplaceLootTablePower.LAST_REPLACED_TABLE_ID));
        } else {
            Optional<LootTable> tableOptional = this.getElementOptional(LootDataType.TABLE, id);
            if(tableOptional.isPresent()) {
                LootTable table = tableOptional.get();
                if(table instanceof IdentifiedLootTable identifiedLootTable) {
                    identifiedLootTable.setId(id, (LootDataManager)(Object)this);
                }
            }
        }
    }
}
