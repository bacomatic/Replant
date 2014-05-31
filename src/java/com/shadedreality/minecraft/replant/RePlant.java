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

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid="mod_Replant", name="Replant", version="0.1")
public class Replant {
	// instance that Forge uses
	@Instance("Replant")
	public static Replant instance;
	
    @SidedProxy(clientSide="com.shadedreality.minecraft.replant.Replant$NullProxy",
                serverSide="com.shadedreality.minecraft.replant.Replant$NullProxy")
    public static NullProxy proxy;
    
	private static final EntityListener entityListener = new EntityListener();
	private static ItemTossListener tossListener = null; // don't allocate unless needed
	
	// Keep these package private
	// TBD: Move this to a config class instead
	static boolean enableItemLifespanTweak = false;
	static int tossedItemLifespan = 100;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	    Configuration config = new Configuration(event.getSuggestedConfigurationFile());
	    config.load();
	    
	    // Load configuration
	    enableItemLifespanTweak = config.get("debugging", "tweakTossedItemLifespan", enableItemLifespanTweak).getBoolean(false);
	    tossedItemLifespan = config.get("debugging", "tossedItemLifespan", tossedItemLifespan).getInt(100);
	    
	    config.save();
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(entityListener);
		
		if (enableItemLifespanTweak) {
		    tossListener = new ItemTossListener();
		    MinecraftForge.EVENT_BUS.register(tossListener);
		}
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
	        if (enableItemLifespanTweak && event.entityItem != null) {
	            event.entityItem.lifespan = tossedItemLifespan;
	        }
	    }
	}
}
