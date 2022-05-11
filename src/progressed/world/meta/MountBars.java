package progressed.world.meta;

import arc.func.*;
import arc.struct.*;
import mindustry.ui.*;
import progressed.util.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;

public class MountBars{
    private OrderedMap<String, Func2<ModularTurretBuild, BaseMount, Bar>> bars = new OrderedMap<>();

    public void add(String name, Func2<ModularTurretBuild, BaseMount, Bar> sup){
        bars.put(name, sup);
    }

    public void remove(String name){
        if(!bars.containsKey(name)) PMUtls.uhOhSpeghettiOh("No bar with name '" + name + "' found; current bars: " + bars.keys().toSeq());
        removeBar(name);
    }

    public Iterable<Func2<ModularTurretBuild, BaseMount, Bar>> list(){
        return bars.values();
    }
}
