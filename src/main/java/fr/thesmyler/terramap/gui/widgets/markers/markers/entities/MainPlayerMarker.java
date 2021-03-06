package fr.thesmyler.terramap.gui.widgets.markers.markers.entities;

import fr.thesmyler.smylibgui.screen.Screen;
import fr.thesmyler.terramap.TerramapRemote;
import fr.thesmyler.terramap.gui.widgets.markers.controllers.MarkerController;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * This class represents a marker for the actual player corresponding to this client
 * 
 * @author SmylerMC
 *
 */
public class MainPlayerMarker extends AbstractPlayerMarker {

	private double playerLongitude, playerLatitude;
	private float playerAzimuth;

	public MainPlayerMarker(MarkerController<?> controller, int downscaleFactor) {
		super(controller, null, downscaleFactor);
	}

	@Override
	public void onUpdate(Screen parent) {
		if(Minecraft.getMinecraft().player == null) {
			parent.scheduleForNextScreenUpdate(() -> parent.removeWidget(this));
			return;
		}
		if(TerramapRemote.getRemote().getProjection() == null) return;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		try {
			double[] lola = TerramapRemote.getRemote().getProjection().toGeo(player.posX, player.posZ);
			this.playerLongitude = lola[0];
			this.playerLatitude = lola[1];
		} catch(OutOfProjectionBoundsException e) {
			this.playerLatitude = this.playerLongitude = Double.NaN;
		}
		try {
			this.playerAzimuth = TerramapRemote.getRemote().getProjection().azimuth(player.posX, player.posZ, player.rotationYaw);
		} catch(OutOfProjectionBoundsException e) {
			this.playerAzimuth = Float.NaN;
		}
		super.onUpdate(parent);
	}

	@Override
	protected ResourceLocation getSkin() {
		return Minecraft.getMinecraft().player.getLocationSkin();
	}

	@Override
	protected double[] getActualCoordinates() {
		return new double[] {this.playerLongitude, this.playerLatitude};
	}

	@Override
	protected float getTransparency() {
		return 1f;
	}

	@Override
	protected boolean showName(boolean hovered) {
		return true;
	}

	@Override
	public ITextComponent getDisplayName() {
		if(Minecraft.getMinecraft().player != null) {
			return Minecraft.getMinecraft().player.getDisplayName();
		} else {
			return new TextComponentString("Missing main player");
		}
	}

	@Override
	public String getIdentifier() {
		String uuid = null;
		if(Minecraft.getMinecraft().player != null) {
			uuid = Minecraft.getMinecraft().player.getUniqueID().toString();
		}
		return this.getControllerId() + ":" + uuid;
	}

	@Override
	protected float getActualAzimuth() throws OutOfProjectionBoundsException {
		return this.playerAzimuth;
	}

	@Override
	protected boolean showDirection(boolean hovered) {
		return true;
	}

}
