package com.tapkacs.raccoons.client.entity;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.HashMap;
import java.util.Map;

public class RaccoonRenderState extends LivingEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> dataMap = new HashMap<>();

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.dataMap;
    }

    // GeckoLib's EntityRenderStateMixin gives every EntityRenderState a concrete addGeckolibData/
    // hasGeckolibData that write straight to a private mixin field instead of going through
    // getDataMap(). Since that mixin method sits on our superclass, normal Java overload
    // resolution would silently prefer it over GeoRenderState's getDataMap()-based default,
    // splitting writes and reads across two disconnected maps. Overriding both here keeps
    // everything on this.dataMap.
    @Override
    public <D> void addGeckolibData(DataTicket<D> dataTicket, D data) {
        this.dataMap.put(dataTicket, data);
    }

    @Override
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return this.dataMap.containsKey(dataTicket);
    }
}
