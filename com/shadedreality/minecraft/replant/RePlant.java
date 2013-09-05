package com.shadedreality.minecraft.replant;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid="RePlant", name="RePlant", version="0.0.1")
@NetworkMod(clientSideRequired=true, serverSideRequired=true)
public class RePlant {
	// instance that Forge uses
	@Instance("RePlant")
	public static RePlant instance;
	
	// where client/server Proxy code is loaded
	@SidedProxy(clientSide="com.shadedreality.minecraft.replant.client.ClientProxy",
			    serverSide="com.shadedreality.minecraft.replant.CommonProxy")
	public static CommonProxy proxy;
	
	private static final EntityListener entityListener = new EntityListener();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
		MinecraftForge.EVENT_BUS.register(entityListener);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
