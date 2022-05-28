package progressed.entities.bullet.energy;

import arc.graphics.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.content.*;
import progressed.content.effects.*;
import progressed.content.effects.UtilFx.*;
import progressed.entities.*;
import progressed.graphics.*;
import progressed.util.*;

public class BeamBulletType extends BulletType{
    public float radius, shake;

    public int crackEffects = 1;
    public float crackStroke = 1.5f, crackWidth = 10f, crackRadius = -1, crackLife = 20f;
    public Color crackColor = PMPal.darkBrown;
    public Effect crackEffect = UtilFx.groundCrack;
    public boolean makePuddles;
    
    public BeamBulletType(float damage, float radius){
        super(0f, damage);
        this.radius = radius;

        despawnEffect = shootEffect = smokeEffect = Fx.none;
        lifetime = 16f;
        keepVelocity = backMove = false;
        hittable = absorbable = false;
        collides = collidesTiles = false;
        collidesGround = true;
        collidesAir = false;
        displayAmmoMultiplier = false;
    }

    @Override
    public float continuousDamage(){
        return damage / 5f * 60f;
    }

    @Override
    public float estimateDPS(){
        //assume firing duration is about 100 by default, may not be accurate there's no way of knowing in this method
        //assume it pierces 3 blocks/units
        return damage * 100f / 5f * 3f;
    }

    @Override
    public void init(){
        super.init();

        if(crackRadius < 0) crackRadius = radius * 2f;
    }

    @Override
    public void update(Bullet b){
        //damage every 5 ticks
        if(b.timer(1, 5f)){
            Damage.damage(b.team, b.x, b.y, radius, damage * b.damageMultiplier(), true, collidesAir, collidesGround);
            if(status != StatusEffects.none) Damage.status(b.team, b.x, b.y, radius, status, statusDuration, collidesAir, collidesGround);

            Tmp.r1.setSize(radius * 2f).setCenter(b.x, b.y);
            Units.nearbyEnemies(b.team, Tmp.r1, u -> {
                if(u.within(b, radius)){
                    if(makePuddles && puddleLiquid != null) Puddles.deposit(u.tileOn(), puddleLiquid, puddleAmount);
                    if(makeFire) Fires.create(u.tileOn());
                }
            });

            PMDamage.trueEachBlock(b.x, b.y, radius, build -> {
                if(build.team == b.team) return;
                if(makePuddles && puddleLiquid != null) Puddles.deposit(build.tileOn(), puddleLiquid, puddleAmount);
                if(makeFire) Fires.create(build.tileOn());
            });

            for(int i = 0; i < crackEffects; i++){
                PMMathf.randomCirclePoint(Tmp.v1, crackRadius).add(b);
                crackEffect.at(b.x, b.y, crackLife, crackColor, new LightningData(Tmp.v1.cpy(), crackStroke, true, crackWidth));
            }
        }

        if(shake > 0){
            Effect.shake(shake, shake, b);
        }
    }

    @Override
    public void draw(Bullet b){
        //Nothing to draw
    }
}
