package com.shadedreality.minecraft.replant;

import net.minecraft.block.Block;
import net.minecraft.block.BlockReed;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemExpireEvent;

public class EntityListener {
	public EntityListener() {
	}

	@ForgeSubscribe
	public void itemDeathEvent(ItemExpireEvent event) {
		Entity entity = event.entity;
		if (entity instanceof EntityItem) {
			EntityItem ei = (EntityItem)entity;
			ItemStack items = ei.getEntityItem();
			
			World world = ei.worldObj;
			int posX = (int)Math.floor(ei.posX);
			int posY = (int)Math.floor(ei.posY);
			int posZ = (int)Math.floor(ei.posZ);
			
			Item item = items.getItem();
			int metadata = item.getMetadata(items.getItemDamage());
			
			if (IPlantable.class.isAssignableFrom(item.getClass())) {
				// seeds, carrot, potato
				IPlantable plantable = (IPlantable)item;
				
				posY--;  // get block immediately below item
				
				int groundType = world.getBlockId(posX, posY, posZ);
	            Block soil = Block.blocksList[groundType];
	            
				// attempt to plant it, duplicating code in ItemSeeds
	            if (soil != null 
	            		&& soil.canSustainPlant(world, posX, posY, posZ, ForgeDirection.UP, plantable)
	            		&& world.isAirBlock(posX, posY + 1, posZ)) {
	                world.setBlock(posX, posY + 1, posZ, plantable.getPlantID(world, posX, posY, posZ));
	                --items.stackSize; // decrement in case something else cancels destruction
	            }
			} else if (ItemBlock.class.isAssignableFrom(item.getClass())) {
				ItemBlock itemBlock = (ItemBlock)item;
				int blockID = itemBlock.getBlockID();
				Block block = Block.blocksList[blockID];
				
				if (IPlantable.class.isAssignableFrom(block.getClass())) {
					// lily is plantable, but we need to "float" it to the surface first since items sink in water
					if (blockID == Block.waterlily.blockID) {
						int waterTop = posY;
						int locID; 
						do {
							waterTop++;
							locID = world.getBlockId(posX, waterTop, posZ);
						} while (locID == Block.waterMoving.blockID
							  || locID == Block.waterStill.blockID);
						
						if (world.isAirBlock(posX, waterTop, posZ)) {
							posY = waterTop;
						} else {
							// cancel, can't grow here
							return;
						}
					}
					
					if (block.canPlaceBlockAt(world, posX, posY, posZ)) {
						// need metadata so the correct trees appear
						world.setBlock(posX, posY, posZ, ((IPlantable)block).getPlantID(world, posX, posY, posZ), metadata, 3);
		                --items.stackSize; // decrement in case something else cancels destruction
					}
				} else {
					// check for melon, pumpkin, lily, cactus
					if (blockID == Block.melon.blockID
				     || blockID == Block.pumpkin.blockID) {
						// get the seed type and plant the seed if we can
						Item seed;
						if (blockID == Block.melon.blockID) {
							seed = Item.melonSeeds;
						} else {
							seed = Item.pumpkinSeeds;
						}
						
						IPlantable plantable = (IPlantable)seed;
						
						posY--;  // get block immediately below item
						
						int groundType = world.getBlockId(posX, posY, posZ);
			            Block soil = Block.blocksList[groundType];
			            
						// attempt to plant it, duplicating code in ItemSeeds
			            if (soil != null 
			            		&& soil.canSustainPlant(world, posX, posY, posZ, ForgeDirection.UP, plantable)
			            		&& world.isAirBlock(posX, posY + 1, posZ)) {
			                world.setBlock(posX, posY + 1, posZ, plantable.getPlantID(world, posX, posY, posZ));
			                --items.stackSize; // decrement in case something else cancels destruction
			            }
					} else if (blockID == Block.cactus.blockID) {
						if (block.canPlaceBlockAt(world, posX, posY, posZ)) {
							world.setBlock(posX, posY, posZ, blockID, metadata, 3);
						}
					}
				}
			} else if (item instanceof ItemReed) {
				// Sugarcane
				BlockReed reed = (BlockReed)Block.reed;
				if (reed.canPlaceBlockAt(world, posX, posY, posZ)) {
					world.setBlock(posX, posY, posZ, reed.blockID, metadata, 3);
				}
			} // else ignore
		}
	}
}
