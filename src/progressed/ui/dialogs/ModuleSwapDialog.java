package progressed.ui.dialogs;

import arc.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import progressed.ui.*;
import progressed.world.blocks.defence.turret.payload.modular.*;
import progressed.world.blocks.defence.turret.payload.modular.ModularTurret.*;
import progressed.world.module.ModuleModule.*;

public class ModuleSwapDialog extends BaseDialog{
    public short selFirst = -1;
    public ModuleSize selSize;
    protected ModularTurretBuild base;

    public ModuleSwapDialog(){
        super("@pm-swap.title");

        buttons.defaults().size(210f, 64f);
        buttons.button("@cancel", Icon.cancel, () -> {
            hide();
            base.resetSwap();
        });
        buttons.button("@confirm", Icon.flipX, () -> {
            hide();
            
            for(int i = 0; i < base.modules.size; i++){
                base.configure(Point2.pack(i, base.modules.get(i).module().mountNumber));
            }
            base.configure(true);
        });

        closeOnBack(() -> base.resetSwap());

        shown(this::rebuild);
    }

    public Dialog show(ModularTurretBuild base){
        this.base = base;
        return show();
    }

    private void rebuild(){
        cont.clear();
        selSize = ModuleSize.small;
        deselect();

        for(ModuleSize mSize : ModuleSize.values()){
            if(base.getMountPos(mSize) == null) continue;
            cont.label(mSize::fullTitle).top().right();
            cont.table(b -> {
                int c = 0;
                for(int i = 0; i < base.getMaxMounts(mSize); i++){
                    int ii = i;
                    Cell<ImageButton> button = b.button(Tex.clear, PMStyles.squarei, 48f, () -> {
                        if(selSize != mSize) deselect();
                        selSize = mSize;
                        if(selFirst != ii){
                            select((short)ii);
                        }else{
                            deselect();
                        }
                    }).update(ib -> ib.setChecked(selSize == mSize && selFirst == ii)).size(64f).left();

                    ShiftedStack pos = new ShiftedStack();
                    Vec2 p = base.getMountPos(mSize)[i];
                    pos.setStackPos(p.x * 4f, p.y * 4f);
                    pos.add(new Image(base.block.fullIcon));

                    TurretModule mount = base.modules.find(m -> m.checkSize(mSize) && m.checkSwap(ii));
                    String num = " (" + (i + 1) + ")";

                    if(mount != null){
                        pos.add(new Image(mount.block().fullIcon));

                        button.get().getStyle().imageUp = new TextureRegionDrawable(mount.icon());
                        button.tooltip(t -> {
                            t.background(Styles.black6).margin(4f);
                            t.label(() -> mount.name() + num);
                            t.row();
                            t.add(pos);
                        });
                    }else{
                        Image m = new Image(((ModularTurret)(base.block)).mountBases[mSize.ordinal()]);
                        m.setColor(((ModularTurret)(base.block)).mountColor);
                        pos.add(m);

                        button.get().clearChildren();
                        button.get().label(() -> String.valueOf(ii + 1));
                        button.tooltip(t -> {
                            t.background(Styles.black6).margin(4f);
                            t.label(() -> Core.bundle.get("empty") + num);
                            t.row();
                            t.add(pos);
                        });
                    }

                    if(c++ % 4 == 3){
                        b.row();
                    }
                }
            }).left().padLeft(16f);
            cont.row();
        }
    }

    private void select(short sel){
        if(selFirst < 0){
            selFirst = sel;
        }else{
            TurretModule m1 = base.modules.find(m -> m.checkSize(selSize) && m.checkSwap(selFirst)),
                m2 = base.modules.find(m -> m.checkSize(selSize) && m.checkSwap(sel));

            if(m1 != null) m1.swap(sel);
            if(m2 != null) m2.swap(selFirst);

            rebuild();
        }
    }

    private void deselect(){
        selFirst = -1;
    }
}
