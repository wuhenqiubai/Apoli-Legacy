package io.github.apace100.apoli.mixin;

//@Mixin(LootDataResolver.class)
public interface LootDataLookupMixin// extends LootDataResolver
{
    /*@Inject(method = "getLootTable", at = @At("HEAD"), cancellable = true)
    private void setTableId(ResourceLocation id, CallbackInfoReturnable<LootTable> cir) {
        ResourceOrIdArgument.LootModifierArgument
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
    }*/
}
