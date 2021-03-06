package fr.thesmyler.terramap.gui.widgets.markers.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.thesmyler.smylibgui.widgets.buttons.ToggleButtonWidget;
import fr.thesmyler.terramap.TerramapRemote;
import fr.thesmyler.terramap.gui.widgets.map.MapWidget;
import fr.thesmyler.terramap.gui.widgets.markers.markers.Marker;
import fr.thesmyler.terramap.gui.widgets.markers.markers.entities.MobMarker;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;

public class MobMarkerController extends MarkerController<MobMarker> {
	
	public static final String ID = "mobs";
	
	protected ToggleButtonWidget button = new ToggleButtonWidget(10, 14, 14,
			102, 108, 102, 122,
			102, 108, 102, 122,
			102, 136, 102, 150,
			this.areMakersVisible(), null, null);

	public MobMarkerController() {
		super(ID, 700, MobMarker.class);
		this.button.setOnActivate(() -> this.setVisibility(true));
		this.button.setOnDeactivate(() -> this.setVisibility(false));
		this.button.setTooltip(I18n.format("terramap.terramapscreen.markercontrollers.buttons.mobs"));
	}

	@Override
	public MobMarker[] getNewMarkers(Marker[] existingMarkers, MapWidget map) {
		if(TerramapRemote.getRemote().getProjection() == null) return new MobMarker[0];
		Map<UUID, Entity> entities = new HashMap<UUID, Entity>();
		for(Entity entity: TerramapRemote.getRemote().getEntities()) {
			if(entity instanceof IMob) {
				entities.put(entity.getPersistentID(), entity);
			}
		}
		for(Marker rawMarker: existingMarkers) {
			MobMarker marker = (MobMarker) rawMarker;
			entities.remove(marker.getEntity().getUniqueID());
		}
		MobMarker[] newMarkers = new MobMarker[entities.size()];
		int i = 0;
		for(Entity entity: entities.values()) {
			newMarkers[i++] = new MobMarker(this, entity);
		}
		return newMarkers;
	}

	@Override
	public boolean showToggleButton() {
		return TerramapRemote.getRemote().allowsMobRadar();
	}

	@Override
	public ToggleButtonWidget getToggleButton() {
		return this.button;
	}

	@Override
	public boolean areMakersVisible() {
		return super.areMakersVisible() && TerramapRemote.getRemote().allowsMobRadar();
	}

}
