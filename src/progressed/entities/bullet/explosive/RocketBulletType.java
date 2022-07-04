package progressed.entities.bullet.explosive;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.content.effects.*;
import progressed.entities.bullet.explosive.BallisticMissileBulletType.*;

import static mindustry.Vars.*;

/** @author MEEP */
public class RocketBulletType extends BasicBulletType{
    public float backSpeed = 1f;
    public float fallDrag = 0.05f, thrustDelay = 20f;
    public float thrusterSize = 4f, thrusterOffset = 8f, thrusterGrowth = 5f;
    public float trailDelay = -1f, trailOffset = 0f;
    public float acceleration = 0.03f;
    public float rotOffset = 0f;

    public float riseStart, riseEnd, targetLayer = -1;

    public Sortf unitSort = Unit::dst2;

    public RocketBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite); //Speed means nothing
        layer = Layer.bullet - 1; //Don't bloom
        keepVelocity = false;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = hitEffect = Fx.blastExplosion;
        hitSound = Sounds.explosion;
        status = StatusEffects.blasted;
        trailRotation = true;
    }

    @Override
    public void init(){
        super.init();
        if(homingDelay < 0) homingDelay = thrustDelay;
        if(targetLayer < 0) targetLayer = layer;
        if(trailDelay < 0) trailDelay = thrusterGrowth;
    }

    @Override
    public void load(){
        super.load();
        backRegion = Core.atlas.find(sprite + "-outline");
    }

    @Override
    public void update(Bullet b){
        RocketData r = (RocketData)b.data;

        if(b.time < thrustDelay && thrustDelay > 0){
            b.vel.scl(Math.max(1f - fallDrag * Time.delta, 0));
        }else{
            if(!r.thrust){
                b.vel.setAngle(r.angle);
                b.vel.setLength(speed);
                r.thrust = true;
            }
            b.vel.scl(Math.max(1f + acceleration * Time.delta, 0));

            super.update(b);
        }
    }

    @Override
    public void updateTrailEffects(Bullet b){
        float angle = ((RocketData)b.data).thrust ? b.rotation() : ((RocketData)b.data).angle,
            x = b.x + Angles.trnsx(angle + 180, trailOffset),
            y = b.y + Angles.trnsy(angle + 180, trailOffset);

        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(x, y, trailRotation ? angle : trailParam, trailColor);
            }
        }

        if(trailInterval > 0f){
            if(b.timer(0, trailInterval)){
                trailEffect.at(x, y, trailRotation ? angle : trailParam, trailColor);
            }
        }
    }

    @Override
    public void updateTrail(Bullet b){
        float angle = ((RocketData)b.data).thrust ? b.rotation() : ((RocketData)b.data).angle,
            x = b.x + Angles.trnsx(angle + 180, trailOffset),
            y = b.y + Angles.trnsy(angle + 180, trailOffset),
            scl = Mathf.curve(b.time, thrustDelay, thrustDelay + thrusterGrowth);

        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            b.trail.update(x, y, trailInterp.apply(b.fin()) * scl * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
        }
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets / 2) * fragSpread);
                Bullet f = fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                if(f.type instanceof BallisticMissileBulletType) f.data = new ArcMissileData(x + Angles.trnsx(a, len), y + Angles.trnsy(a, len));
            }
        }
    }

    @Override
    public void draw(Bullet b){
        if(b.data instanceof RocketData r){
            float angle = r.thrust ? b.rotation() : r.angle;
            Draw.z(getLayer(b));
            float z = Draw.z();

            if(b.time >= thrustDelay || thrustDelay <= 0){ //Engine draw code stolen from units
                float scale = Mathf.curve(b.time, thrustDelay, thrustDelay + thrusterGrowth);
                float offset = thrusterOffset / 2f + thrusterOffset / 2f * scale;

                //drawTrail but with the above variables
                if(trailLength > 0 && b.trail != null){
                    Draw.z(z - 0.0001f);
                    b.trail.draw(b.team.color, trailWidth * scale);
                    Draw.z(z);
                }

                Draw.color(b.team.color);
                Fill.circle(
                    b.x + Angles.trnsx(angle + 180, offset),
                    b.y + Angles.trnsy(angle + 180, offset),
                    (thrusterSize + Mathf.absin(Time.time, 2f, thrusterSize / 4f)) * scale
                );
                Draw.color(Color.white);
                Fill.circle(
                    b.x + Angles.trnsx(angle + 180, offset - thrusterSize / 2f),
                    b.y + Angles.trnsy(angle + 180, offset - thrusterSize / 2f),
                    (thrusterSize + Mathf.absin(Time.time, 2f, thrusterSize / 4f)) / 2f  * scale
                );
                Draw.color();
            }

            Draw.z(z - 0.01f);
            Draw.rect(backRegion, b.x, b.y, angle - 90f + rotOffset);
            Draw.z(z);
            Draw.rect(frontRegion, b.x, b.y, angle - 90f + rotOffset);
            Draw.reset();
        }
    }

    @Override
    public void removed(Bullet b){
        if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
            UtilFx.rocketTrailFade.at(b.x, b.y, trailWidth, b.team.color, new RocketTrailData(b.trail.copy(), getLayer(b)));
        }
    }

    public float getLayer(Bullet b){
        float progress = Mathf.curve(b.time, riseStart, riseEnd);
        return Mathf.lerp(layer, targetLayer, progress);
    }

    @Override
    public Bullet create(Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data){
        Bullet bullet = super.create(owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data);
        if(backSpeed != 0f){
            bullet.initVel(angle, -backSpeed * velocityScl);
            if(backMove){
                bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
            }else{
                bullet.set(x, y);
            }
            if(keepVelocity && owner instanceof Velc v) bullet.vel.add(v.vel());
        }
        bullet.data = new RocketData(angle);
        return bullet;
    }

    public static class RocketData{ //I could use data for lock and fdata for angle, but this looks nicer
        float angle;
        boolean thrust;

        public RocketData(float angle){
            this.angle = angle;
        }
    }

    public static class RocketTrailData{
        public Trail trail;
        public float layer;

        public RocketTrailData(Trail trail, float layer){
            this.trail = trail;
            this.layer = layer;
        }
    }
}
