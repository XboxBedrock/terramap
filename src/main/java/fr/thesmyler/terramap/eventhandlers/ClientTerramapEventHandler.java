package fr.thesmyler.terramap.eventhandlers;

import fr.thesmyler.smylibgui.SmyLibGui;
import fr.thesmyler.smylibgui.event.HudScreenInitEvent;
import fr.thesmyler.smylibgui.screen.Screen;
import fr.thesmyler.smylibgui.screen.TestScreen;
import fr.thesmyler.terramap.GeoServices;
import fr.thesmyler.terramap.TerramapMod;
import fr.thesmyler.terramap.TerramapRemote;
import fr.thesmyler.terramap.config.TerramapConfig;
import fr.thesmyler.terramap.gui.HudScreenHandler;
import fr.thesmyler.terramap.input.KeyBindings;
import io.github.terra121.projection.GeographicProjection;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import io.github.terra121.util.CardinalDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Event handler for the physical client
 *
 */
@SideOnly(Side.CLIENT)
public class ClientTerramapEventHandler {
	
	private boolean testScreenWasShown = false;
	private boolean configWasFixed = false;
	
	@SubscribeEvent
	public void onRenderHUD(RenderGameOverlayEvent.Text event) {
		if(Minecraft.getMinecraft().gameSettings.showDebugInfo) {
			GeographicProjection proj = TerramapRemote.getRemote().getProjection();
			if(proj != null) {
				event.getLeft().add("");
				double x = Minecraft.getMinecraft().player.posX;
				double z = Minecraft.getMinecraft().player.posZ;
				float yaw = Minecraft.getMinecraft().player.rotationYaw;
				try {
					double[] coords = proj.toGeo(x, z);
					float azimuth = proj.azimuth(x, z, yaw);
					String lon = GeoServices.formatGeoCoordForDisplay(coords[0]);
					String lat = GeoServices.formatGeoCoordForDisplay(coords[1]);
					String azimuthStr = GeoServices.formatAzimuthForDisplay(azimuth);
					String cardinal = CardinalDirection.azimuthToFacing(azimuth).realName();
					event.getLeft().add("Position: " + lat + "° " + lon + "° Looking at: " + azimuthStr + "° (" + cardinal + ")");
				} catch(OutOfProjectionBoundsException e) {
					event.getLeft().add("Out of projection bounds");
				}
			}
			event.getLeft().add("Terramap world UUID: " + TerramapRemote.getRemote().getWorldUUID());
			event.getLeft().add("Terramap proxy UUID: " + TerramapRemote.getRemote().getProxyUUID());
		}
	}

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
    	KeyBindings.checkBindings();
    }
    
	@SubscribeEvent
	public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
		Minecraft.getMinecraft().addScheduledTask(TerramapRemote::resetRemote); // This event is called from the network thread
	}

	@SubscribeEvent
	public void onClientConnected(ClientConnectedToServerEvent event) {
		Minecraft.getMinecraft().addScheduledTask(() -> TerramapRemote.getRemote().setRemoteIdentifier()); // This event is called from the network thread
	}
	
	@SubscribeEvent
	public void onChangeDimension(PlayerChangedDimensionEvent event) {
		// Not called on client...
		TerramapMod.logger.info(event.player.world.isRemote);
		if(event.player.world.isRemote) {
			TerramapRemote.getRemote().resetWorld();
		}
	}
	
	@SubscribeEvent
	public void onHudInit(HudScreenInitEvent event) {
		HudScreenHandler.init(event.getHudScreen());
		TerramapRemote.getRemote().setupMaps();
	}
	
	@SubscribeEvent
	public void onGuiScreenInit(InitGuiEvent event) {
		if(event.getGui() instanceof GuiMainMenu && !configWasFixed) {
			/* 
			 * Unfortunately, Forge's ConfigManager does not let us modify our config when the game is still loading and 
			 * and calling ConfigManager::sync only injects the file's value into the fields instead of saving them to disk,
			 * which is why we have to do it once the game is fully loaded.
			 * 
			 * This is called on the physical server by TerramapServerProxy::onServerStarting.
			 */
		    TerramapConfig.update(); // Update if invalid values were left by old versions
		    configWasFixed = true;
		}
		if(SmyLibGui.debug && !testScreenWasShown && !(event.getGui() instanceof Screen)) {
			Minecraft.getMinecraft().displayGuiScreen(new TestScreen(event.getGui()));
			this.testScreenWasShown = true;
		} else if(event.getGui() instanceof GuiDownloadTerrain) {
			TerramapRemote.getRemote().resetWorld();
		}
	}
	
}
