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

import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid="mod_Replant", name="Replant", version="0.1")
public class Replant {
	// instance that Forge uses
	@Instance("Replant")
	public static Replant instance;
	
    @SidedProxy(clientSide="com.shadedreality.minecraft.replant.Replant$NullProxy",
                serverSide="com.shadedreality.minecraft.replant.Replant$NullProxy")
    public static NullProxy proxy;
    
	private static final EntityListener entityListener = new EntityListener();
	private static ItemTossListener tossListener = new ItemTossListener();
	
	private static String TWEAK_TOSSED_ITEM_LIFESPAN_RULE = "replantTweakTossedItemLifespan";
	private static String TOSSED_ITEM_LIFESPAN_RULE = "replantTossedItemLifespan";
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(entityListener);
	    MinecraftForge.EVENT_BUS.register(tossListener);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	public static class NullProxy {
	    public void registerRenderers() {
	        // not needed
	    }
	}
	
	public static class ItemTossListener {
	    @SubscribeEvent
	    public void itemTossedEvent(ItemTossEvent event) {
	    	World world = event.entityItem.getEntityWorld();
	    	GameRules rules = world.getGameRules();
	    	if (rules.getGameRuleBooleanValue(TWEAK_TOSSED_ITEM_LIFESPAN_RULE)) {
	    		int lifeSpan = rules.getInt(TOSSED_ITEM_LIFESPAN_RULE);
	    		if (lifeSpan > 0) {
	    			System.out.println("Adjusting tossed item lifespan: "+lifeSpan);
	    			event.entityItem.lifespan = lifeSpan;
	    		}
	    	}
	    }
	}
}
