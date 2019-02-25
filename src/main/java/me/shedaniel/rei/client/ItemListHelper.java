package me.shedaniel.rei.client;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IItemRegisterer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ItemListHelper implements IItemRegisterer {
    
    private final List<ItemStack> itemList = Lists.newLinkedList();
    
    @Override
    public List<ItemStack> getItemList() {
        return Collections.unmodifiableList(itemList);
    }
    
    @Deprecated
    @Override
    public List<ItemStack> getModifiableItemList() {
        return itemList;
    }
    
    @Override
    public Optional<NonNullList<ItemStack>> getAlterativeStacks(Item item) {
        try {
            NonNullList<ItemStack> list = NonNullList.create();
            list.add(item.getDefaultInstance());
            item.fillItemGroup(item.getGroup(), list);
            TreeSet<ItemStack> stackSet = list.stream().collect(Collectors.toCollection(() -> new TreeSet<ItemStack>((p1, p2) -> ItemStack.areItemStacksEqual(p1, p2) ? 0 : 1)));
            list = NonNullList.create();
            stackSet.forEach(list::add);
            if (!list.isEmpty())
               return Optional.of(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            NonNullList<ItemStack> list = NonNullList.create();
            list.add(item.getDefaultInstance());
            return Optional.of(list);
        } catch (Exception e) {
        }
        return Optional.empty();
    }
    
    @Override
    public void registerItemStack(Item afterItem, ItemStack stack) {
        if (!stack.isEmpty() && !alreadyContain(stack))
            if (afterItem == null || afterItem.equals(Items.AIR))
                itemList.add(stack);
            else {
                int last = itemList.size();
                for(int i = 0; i < itemList.size(); i++)
                    if (itemList.get(i).getItem().equals(afterItem))
                        last = i + 1;
                itemList.add(last, stack);
            }
    }
    
}
