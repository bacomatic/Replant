/*
 * Copyright (c) 2013, 2014, Shaded Reality
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.shadedreality.minecraft.replant;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockReed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;

public class EntityListener {
    private static class EntityRegistrar {
        // Cached Block types, to avoid hammering the block and item registries
        // These are initialized lazily, the VM won't run the static initializer
        // until this inner class is accessed, so as long as we only call it in
        // the event handler then all these block types should already be registered
        public static final Block waterMoving;
        public static final Block waterStill;
        public static final BlockReed reeds;

        public static final ItemSeeds melonSeeds;
        public static final ItemSeeds pumpkinSeeds;
        
        static {
            waterMoving = Block.getBlockFromName("flowing_water");
            waterStill = Block.getBlockFromName("water");
            reeds = (BlockReed) Block.getBlockFromName("reeds");

            melonSeeds = (ItemSeeds) Item.itemRegistry.getObject("melon_seeds");
            pumpkinSeeds = (ItemSeeds) Item.itemRegistry.getObject("pumpkin_seeds");
        }
    }

    public EntityListener() {
    }

    @SubscribeEvent
    public void itemExpireEvent(ItemExpireEvent event) {
        Entity entity = event.entity;
        if (entity instanceof EntityItem) {
            EntityItem ei = (EntityItem) entity;
            ItemStack items = ei.getEntityItem();

            World world = ei.worldObj;
            int posX = (int) Math.floor(ei.posX);
            int posY = (int) Math.floor(ei.posY);
            int posZ = (int) Math.floor(ei.posZ);

            Item item = items.getItem();
            Class<?> itemClass = item.getClass();
            int metadata = item.getMetadata(items.getItemDamage());

            if (IPlantable.class.isAssignableFrom(itemClass)) {
                // seeds, carrot, potato
                IPlantable plantable = (IPlantable) item;

                posY--; // get block immediately below item

                Block soil = world.getBlock(posX, posY, posZ);
                // attempt to plant it, duplicating code in ItemSeeds
                if (soil != null
                        && soil.canSustainPlant(world, posX, posY, posZ, ForgeDirection.UP, plantable)
                        && world.isAirBlock(posX, posY + 1, posZ)) {
                    world.setBlock(posX, posY + 1, posZ, plantable.getPlant(world, posX, posY, posZ));
                    --items.stackSize; // decrement in case something else
                                       // cancels destruction
                }
            } else if (ItemBlock.class.isAssignableFrom(itemClass)) {
                ItemBlock itemBlock = (ItemBlock) item;
                Block block = itemBlock.field_150939_a; // ugh...
                Class<?> blockClass = block.getClass();

                if (IPlantable.class.isAssignableFrom(blockClass)) {
                    // lily is plantable, but we need to "float" it to the
                    // surface first since items sink in water
                    if (BlockLilyPad.class.isAssignableFrom(blockClass)) {
                        int waterTop = posY;
                        Block worldBlock;
                        do {
                            waterTop++;
                            worldBlock = world.getBlock(posX, waterTop, posZ);
                        } while (Block.isEqualTo(worldBlock, EntityRegistrar.waterMoving)
                              || Block.isEqualTo(worldBlock, EntityRegistrar.waterStill));

                        if (world.isAirBlock(posX, waterTop, posZ)) {
                            posY = waterTop;
                        } else {
                            // cancel, can't grow here
                            return;
                        }
                    }

                    if (block.canPlaceBlockAt(world, posX, posY, posZ)) {
                        // need metadata so the correct trees appear
                        world.setBlock(posX, posY, posZ, ((IPlantable) block)
                                .getPlant(world, posX, posY, posZ), metadata, 3);
                        --items.stackSize; // decrement in case something else cancels destruction
                    }
                } else {
                    // check for melon, pumpkin, cactus
                    // They are neither plantable nor seeds, so we have to handle
                    // them specially
                    if (BlockPumpkin.class.isAssignableFrom(blockClass)
                            || BlockMelon.class.isAssignableFrom(blockClass)) {
                        // get the seed type and plant the seed if we can
                        Item seed;
                        if (BlockMelon.class.isAssignableFrom(blockClass)) {
                            seed = EntityRegistrar.melonSeeds;
                        } else {
                            seed = EntityRegistrar.pumpkinSeeds;
                        }
                        
                        // Bummer, we can't just use ItemSeed.onItemUse or we'll get an NPE
                        // since we don't have a player
                        IPlantable plantable = (IPlantable) seed;
                        posY--; // get block immediately below item
                        Block soil = world.getBlock(posX, posY, posZ);

                        // attempt to plant it, duplicating code in ItemSeeds
                        // TBD: Could we just use ItemSeeds?
                        if (soil != null
                                && soil.canSustainPlant(world, posX, posY, posZ, ForgeDirection.UP, plantable)
                                && world.isAirBlock(posX, posY + 1, posZ)) {
                            world.setBlock(posX, posY + 1, posZ, plantable.getPlant(world, posX, posY, posZ));
                            --items.stackSize; // decrement in case something
                                               // else cancels destruction
                        }
                    } else if (BlockCactus.class.isAssignableFrom(blockClass)) {
                        if (block.canPlaceBlockAt(world, posX, posY, posZ)) {
                            world.setBlock(posX, posY, posZ, block, metadata, 3);
                        }
                    }
                }
            } else if (ItemReed.class.isAssignableFrom(itemClass)) {
                // Sugarcane
                // ItemReed's block field is private, so we have to ask
                // BlockReed directly
                if (EntityRegistrar.reeds.canPlaceBlockAt(world, posX, posY, posZ)) {
                    world.setBlock(posX, posY, posZ, EntityRegistrar.reeds, metadata, 3);
                }
            } // else ignore
        }
    }
}
