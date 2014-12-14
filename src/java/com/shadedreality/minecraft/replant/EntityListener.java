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

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.BlockMelon;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockReed;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

    private boolean isWater(Block b) {
    	return Block.isEqualTo(EntityRegistrar.waterMoving, b) ||
    		   Block.isEqualTo(EntityRegistrar.waterStill, b);
    }

    private boolean plantPlantable(World world, BlockPos where, IPlantable seed) {
    	BlockPos groundPos = where.down();
        Block soil = world.getBlockState(groundPos).getBlock();
        // attempt to plant it
        if (soil != null
                && soil.canSustainPlant(world, groundPos, EnumFacing.UP, seed) 
                && world.isAirBlock(where)) {
            world.setBlockState(where, seed.getPlant(world, where));
            return true;
        }
        return false;
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

            BlockPos entityPos = new BlockPos(posX, posY, posZ);
            // if the item occupies the same space as a block (e.g., sitting on soul sand) then
            // bump entityPos up by one
            Block eb = world.getBlockState(entityPos).getBlock();
            if (!world.isAirBlock(entityPos) && !isWater(eb)) {
            	entityPos = entityPos.up();
            	if (!world.isAirBlock(entityPos) && !isWater(eb)) {
            		// no valid block
            		return;
            	}
            }
            BlockPos groundPos = entityPos.down();

        	// hack to get melon block/slices and pumpkin to plant
        	if (ItemFood.class.isAssignableFrom(itemClass)) {
        		if (item.getUnlocalizedName().equalsIgnoreCase("item.melon")) {
        			item = EntityRegistrar.melonSeeds;
        		}
        	} else if (ItemBlock.class.isAssignableFrom(itemClass)) {
        		Block block = ((ItemBlock)item).block;
        		Class<?> blockClass = block.getClass();
                if (BlockPumpkin.class.isAssignableFrom(blockClass)
                    || BlockMelon.class.isAssignableFrom(blockClass)) {
	                // get the seed type and plant the seed if we can
	                Item seed = null;
	                if (BlockMelon.class.isAssignableFrom(blockClass)) {
	                    item = EntityRegistrar.melonSeeds;
	                } else if (BlockPumpkin.class.isAssignableFrom(blockClass)) {
	                    item = EntityRegistrar.pumpkinSeeds;
	                }
                }
        	}
        	// update in case it changed
			itemClass = item.getClass();

            // use isAssignableFrom to catch sub-classes too
            if (IPlantable.class.isAssignableFrom(itemClass)) {
                // seeds, carrot, potato
            	if (plantPlantable(world, entityPos, (IPlantable)item)) {
	                --items.stackSize; // decrement in case something else cancels destruction
            	}
            } else if (ItemBlock.class.isAssignableFrom(itemClass) ||
            		   ItemReed.class.isAssignableFrom(itemClass)) {
            	Block block = null;
            	Class<?> blockClass = null;
            	if (ItemReed.class.isAssignableFrom(itemClass)) {
            		block = EntityRegistrar.reeds;
            	} else {
            		block = ((ItemBlock)item).block;
            	}
            	
            	if (block != null) {
            		blockClass = block.getClass();
            	}
            	
                // Cactus and reed plant the same as IPlantable blocks
            	// IPlantable ItemBlocks are things like saplings, flowers, mushrooms, etc
            	// that plant themselves (not seeds)
                if (blockClass != null &&
                	(IPlantable.class.isAssignableFrom(blockClass) ||
                	 BlockCactus.class.isAssignableFrom(blockClass) ||
                	 BlockReed.class.isAssignableFrom(blockClass))) {
                	
                    // lily is plantable, but we need to "float" it to the
                    // surface first since items sink in water
                    if (BlockLilyPad.class.isAssignableFrom(blockClass)) {
                        int waterTop = posY;
                        Block worldBlock;
                        BlockPos waterPos = entityPos;
                        do {
                        	waterPos = waterPos.up();
                            worldBlock = world.getBlockState(waterPos).getBlock();
                        } while (isWater(worldBlock));

                        if (world.isAirBlock(waterPos)) {
                            entityPos = waterPos; // swap new position to plant
                        } else {
                            // cancel, can't grow here
                            return;
                        }
                    }

                    // Handle double plants here or we'll just end up planting the bottom part of the plant
                    if (block.canPlaceBlockAt(world, entityPos)) {
	                    if (BlockDoublePlant.class.isAssignableFrom(blockClass)) {
	                        BlockDoublePlant bdp = (BlockDoublePlant)block;
	                        // metadata bit 7 means upper half, only plant lower half
	                        BlockDoublePlant.EnumPlantType type = BlockDoublePlant.EnumPlantType.byMetadata(metadata & 7);
	                        bdp.placeAt(world, entityPos, type, 3);
	                    } else {
	                    	// position is irrelevant to getPlant
	                        world.setBlockState(entityPos, block.getStateFromMeta(metadata));
	                    }
                        --items.stackSize; // decrement in case something else cancels destruction
                    }
                }
            } // else ignore
        }
    }
}
