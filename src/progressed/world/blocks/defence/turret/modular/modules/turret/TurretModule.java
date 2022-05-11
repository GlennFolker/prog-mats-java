package progressed.world.blocks.defence.turret.modular.modules.turret;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.Units.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.meta.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.blocks.payloads.ModulePayload.*;

public class TurretModule extends ReloadTurretModule{
    public boolean targetAir = true, targetGround = true, targetBlocks = true,
        targetEnemies = true, targetHealing;
    public boolean leadTargets = true, accurateDelay;
    public boolean moveWhileShooting = true;

    public float shootY;
    public int shots = 1;
    public boolean countAfter;
    public float barrelSpacing, angleSpread;
    public float shootShake;
    public float xRand;
    /** Currently used for artillery only. */
    public float minRange = 0f;
    public float burstSpacing = 0;
    public int barrels = 1;
    public float inaccuracy, velocityInaccuracy;

    public Effect shootEffect = Fx.none;
    public Effect smokeEffect = Fx.none;
    public Sound shootSound = Sounds.shoot;
    public Effect ammoUseEffect = Fx.none;
    public boolean alternate = false;
    public float ammoEjectX = 1f, ammoEjectY = -1f;
    public boolean rotate = true;

    public float chargeTime = -1f;
    public int chargeEffects = 5;
    public float chargeMaxDelay = 10f;
    public Effect chargeEffect = Fx.none;
    public Effect chargeBeginEffect = Fx.none;
    public Sound chargeSound = Sounds.none;

    public Sortf unitSort = UnitSorts.closest;

    public TurretModule(String name, ModuleSize size){
        super(name, size);
        mountType = TurretMount::new;
        recoil = size();
        shootY = size() * Vars.tilesize / 2f;
    }

    public TurretModule(String name){
        this(name, ModuleSize.small);
    }

    @Override
    public void init(){
        if(barrels < 1) barrels = 1; //Do not
        if(!targetEnemies) targetHealing = true; //At least shoot at *something*

        super.init();
    }

    @Override
    public void setStats(Stats stats){
        super.setStats(stats);

        stats.add(Stat.inaccuracy, (int)inaccuracy, StatUnit.degrees);
        stats.add(Stat.reload, 60f / (reload) * shots, StatUnit.perSecond);
        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
    }

    public boolean canHeal(TurretMount mount){
        return targetHealing && hasAmmo(mount) && peekAmmo(mount).collidesTeam && peekAmmo(mount).healPercent > 0;
    }

    protected boolean validateTarget(ModularTurretBuild parent, TurretMount mount){
        return !Units.invalidateTarget(mount.target, canHeal(mount) ? Team.derelict : parent.team, mount.x, mount.y) || (playerControl && parent.isControlled()) || (logicControl && parent.logicControlled());
    }

    @Override
    public boolean isActive(ModularTurretBuild parent, BaseMount mount){
        TurretMount m = (TurretMount)mount;
        return isDeployed(m) && (m.target != null || m.wasShooting);
    }

    @Override
    public boolean usePower(ModularTurretBuild parent, BaseMount mount){
        return super.usePower(parent, mount) && shouldReload(parent, (TurretMount)mount);
    }

    @Override
    public void targetPosition(BaseTurretMount m, Posc pos){
        TurretMount mount = (TurretMount)m;
        if(!hasAmmo(mount) || pos == null) return;
        BulletType bullet = peekAmmo(mount);

        var offset = Tmp.v1.setZero();

        if(leadTargets){
            //when delay is accurate, assume unit has moved by chargeTime already
            if(accurateDelay && pos instanceof Hitboxc h){
                offset.set(h.deltaX(), h.deltaY()).scl(chargeTime / Time.delta);
            }

            recoilOffset.trns(mount.rotation, shootY - mount.curRecoil);
            mount.targetPos.set(Predict.intercept(mount, pos, offset.x, offset.y, bullet.speed <= 0.01f ? 99999999f : bullet.speed));
        }

        if(mount.targetPos.isZero()){
            mount.targetPos.set(pos);
        }
    }

    @Override
    public void update(ModularTurretBuild parent, BaseMount m){
        super.update(parent, m);
        if(!isDeployed(m)) return;

        TurretMount mount = (TurretMount)m;

        if(!validateTarget(parent, mount)) mount.target = null;

        if(mount.bullet != null && !mount.bullet.isAdded()) mount.bullet = null;

        mount.wasShooting = false;

        mount.curRecoil = Mathf.lerpDelta(mount.curRecoil, 0f, restitution);
        mount.heat = Mathf.lerpDelta(mount.heat, 0f, cooldown);

        if(hasAmmo(mount)){
            if(Float.isNaN(mount.reloadCounter)) mount.reloadCounter = 0;

            if(validateTarget(parent, mount)){
                boolean canShoot = true;

                if(playerControl && parent.isControlled()){ //player behavior
                    mount.targetPos.set(parent.unit.aimX(), parent.unit.aimY());
                    canShoot = parent.unit.isShooting();
                }else if(logicControl && parent.logicControlled()){ //logic behavior
                    canShoot = parent.logicShooting;
                }else{ //default AI behavior
                    targetPosition(mount, mount.target);

                    if(Float.isNaN(mount.rotation)) mount.rotation = 0;
                }

                float targetRot = Angles.angle(mount.x, mount.y, mount.targetPos.x, mount.targetPos.y);

                if(shouldTurn(mount)){
                    turnToTarget(parent, mount, targetRot);
                }

                if(!mount.isShooting && (!rotate || Angles.angleDist(mount.rotation, targetRot) < shootCone) && canShoot){
                    mount.wasShooting = true;
                    updateShooting(parent, mount);
                }
            }
        }

        updateCharging(parent, mount);
    }

    @Override
    public void updateCooling(ModularTurretBuild parent, BaseTurretMount mount){
        if(((TurretMount)mount).charge <= 0f) super.updateCooling(parent, mount);
    }

    public void updateShooting(ModularTurretBuild parent, TurretMount mount){
        if(!shouldReload(parent, mount)) return;
        mount.reloadCounter += peekAmmo(mount).reloadMultiplier * delta(parent);

        if(mount.reloadCounter >= reload){
            BulletType type = peekAmmo(mount);

            shoot(parent, mount, type);

            mount.reloadCounter %= reload;
        }

        if(acceptCoolant){
            updateCooling(parent, mount);
        }
    }

    public void updateCharging(ModularTurretBuild parent, TurretMount mount){
        if(mount.charging){
            mount.charge += Time.delta;

            if(mount.charge >= chargeTime){
                mount.charging = false;
                chargeShot(parent, mount);
            }

            mount.charge = 0;
        }
    }

    public void shoot(ModularTurretBuild parent, TurretMount mount, BulletType type){
        float x = mount.x,
            y = mount.y,
            rot = mount.rotation;

        //when charging is enabled, use the charge shoot pattern
        if(chargeTime > 0){
            useAmmo(parent, mount);
            mount.chargeShot = type;

            shootOffset.trns(rot, shootY);
            chargeBeginEffect.at(x + shootOffset.x, y + shootOffset.y, rot);
            chargeSound.at(x + shootOffset.x, y + shootOffset.y, 1);

            for(int i = 0; i < chargeEffects; i++){
                Time.run(Mathf.random(chargeMaxDelay), () -> {
                    if(parent.dead) return;
                    shootOffset.trns(rot, shootY);
                    chargeEffect.at(x + shootOffset.x, y + shootOffset.y, rot);
                });
            }

            mount.charging = true;
        }else{
            for(int i = 0; i < shots; i++){
                int ii = i;
                if(burstSpacing > 0.0001f){
                    mount.isShooting = true;
                    Time.run(burstSpacing * i, () -> {
                        mount.isShooting = true;
                        if(!mount.valid(parent) || !hasAmmo(mount)){
                            mount.isShooting = false;
                            return;
                        }
                        basicShoot(parent, mount, peekAmmo(mount), ii);
                        effects(mount, peekAmmo(mount));
                        useAmmo(parent, mount);
                        if(ii == shots - 1) mount.isShooting = false;
                    });
                }else{
                    basicShoot(parent, mount, type, i);
                    if(barrels > 1 && !countAfter) effects(mount, peekAmmo(mount));
                }
            }

            if(burstSpacing <= 0.0001f){
                if(barrels == 1 || countAfter) effects(mount, type);
                useAmmo(parent, mount);
            }
            if(countAfter) mount.shotCounter++;
        }
    }

    public void basicShoot(ModularTurretBuild parent, TurretMount mount, BulletType type, int count){
        float rot = mount.rotation;
        float b = (mount.shotCounter % barrels) - (barrels - 1) / 2f;

        shootOffset.trns(rot - 90, barrelSpacing * b + Mathf.range(xRand), shootY - mount.curRecoil);
        bullet(parent, mount, type, rot + Mathf.range(inaccuracy + type.inaccuracy) + (count - (int)(shots / 2f)) * angleSpread);

        mount.curRecoil = recoil;
        mount.heat = 1f;
        if(!countAfter) mount.shotCounter++;
    }

    public void chargeShot(ModularTurretBuild parent, TurretMount mount){
        if(!hasAmmo(mount)) return;
        BulletType type = mount.chargeShot == null ? peekAmmo(mount) : mount.chargeShot;

        shootOffset.trns(mount.rotation, shootY - mount.curRecoil);
        bullet(parent, mount, type, mount.rotation + Mathf.range(inaccuracy + type.inaccuracy));
        effects(mount, type);
    }

    protected void bullet(ModularTurretBuild parent, TurretMount mount, BulletType type, float angle){
        float x = mount.x, y = mount.y;
        float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(x + shootOffset.x, y + shootOffset.y, mount.targetPos.x, mount.targetPos.y) / type.range, minRange / type.range, range / type.range) : 1f;

        mount.bullet = type.create(parent, parent.team, x + shootOffset.x, y + shootOffset.y, angle, 1f + Mathf.range(velocityInaccuracy), lifeScl);
    }

    protected void effects(TurretMount mount, BulletType type){
        float x = mount.x, y = mount.y;

        Effect fshootEffect = shootEffect == Fx.none ? type.shootEffect : shootEffect;
        Effect fsmokeEffect = smokeEffect == Fx.none ? type.smokeEffect : smokeEffect;

        fshootEffect.at(x + shootOffset.x, y + shootOffset.y, mount.rotation);
        fsmokeEffect.at(x + shootOffset.x, y + shootOffset.y, mount.rotation);
        shootSound.at(x + shootOffset.x, y + shootOffset.y, Mathf.random(0.9f, 1.1f));

        if(shootShake > 0){
            Effect.shake(shootShake, shootShake, x, y);
        }

        mount.curRecoil = recoil;
    }

    protected void ejectEffects(TurretMount mount){
        float scl = Mathf.sign(alternate && mount.shotCounter % 2 == 0);

        recoilOffset.trns(mount.rotation - 90, ammoEjectX, ammoEjectY);
        ammoUseEffect.at(mount.x + recoilOffset.x, mount.y + recoilOffset.y, mount.rotation * scl);
    }

    /** Consume ammo and return a type. */
    public BulletType useAmmo(ModularTurretBuild parent, TurretMount mount){
        if(parent.cheating()) return peekAmmo(mount);

        AmmoEntry entry = mount.ammo.peek();
        entry.amount -= 1;
        if(entry.amount <= 0) mount.ammo.pop();
        mount.totalAmmo = Math.max(mount.totalAmmo - 1, 0);
        ejectEffects(mount);
        return entry.type();
    }

    /** @return the ammo type that will be returned if useAmmo is called. */
    public BulletType peekAmmo(TurretMount mount){
        return mount.ammo.peek().type();
    }

    /** @return whether the turret has ammo. */
    @Override
    public boolean hasAmmo(TurretMount mount){
        return mount.ammo.size > 0;
    }

    public boolean shouldTurn(TurretMount mount){
        return !mount.charging && (moveWhileShooting || !mount.isShooting);
    }

    public void turnToTarget(ModularTurretBuild parent, TurretMount mount, float targetRot){
        if(rotate){
            mount.rotation = Angles.moveToward(mount.rotation, targetRot, rotateSpeed * edelta(parent));
        }else{
            mount.rotation = targetRot;
        }
    }

    @Override
    public void findTarget(ModularTurretBuild parent, BaseTurretMount m){
        TurretMount mount = (TurretMount)m;
        if(!hasAmmo(mount)) return;
        float x = mount.x,
            y = mount.y;
        if(targetEnemies){
            if(targetAir && !targetGround){
                mount.target = Units.bestEnemy(parent.team, x, y, range, e -> !e.dead() && !e.isGrounded(), unitSort);
            }else{
                mount.target = Units.bestTarget(parent.team, x, y, range, e -> !e.dead() && (e.isGrounded() || targetAir) && (!e.isGrounded() || targetGround), b -> targetBlocks, unitSort);

                if(mount.target == null && canHeal(mount)){
                    mount.target = Units.findAllyTile(parent.team, x, y, range, b -> b.damaged() && b != parent);
                }
            }
        }else{
            mount.target = Units.findAllyTile(parent.team, x, y, range, b -> b.damaged() && b != parent);
        }
    }

    @Override
    public void draw(ModularTurretBuild parent, BaseMount m){
        BaseTurretMount mount = (BaseTurretMount)m;
        float x = mount.x,
            y = mount.y,
            rot = rotate ? mount.rotation - 90f : 0;

        if(mount.progress < deployTime){
            Draw.draw(Draw.z(), () -> {
                PMDrawf.blockBuildCenter(x, y, region, rot, mount.progress / deployTime);
                if(buildTop) PMDrawf.blockBuildCenter(x, y, topRegion, rot, mount.progress / deployTime);
            });
            return;
        }

        shootOffset.trns(rot + 90f, rotate ? -mount.curRecoil : 0f);

        Drawf.shadow(region, x + shootOffset.x - elevation, y + shootOffset.y - elevation, rot);
        applyColor(parent, mount);
        Draw.rect(region, x + shootOffset.x, y + shootOffset.y, rot);

        if(heatRegion.found() && mount.heat > 0.001f){
            Draw.color(heatColor, mount.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, x + shootOffset.x, y + shootOffset.y, rot);
            Draw.blend();
            Draw.color();
        }

        if(liquidRegion.found()){
            Drawf.liquid(liquidRegion, x + shootOffset.x, y + shootOffset.y, mount.liquids.currentAmount() / liquidCapacity, mount.liquids.current().color, rot);
        }

        if(topRegion.found()){
            Draw.z(Layer.turret + topLayerOffset);
            Draw.rect(topRegion, x + shootOffset.x, y + shootOffset.y, rot);
        }
        Draw.mixcol();
    }

    @Override
    public void drawPayload(ModulePayloadBuild payload){
        super.drawPayload(payload);

        if(topRegion.found()){
            Draw.rect(topRegion, payload.x, payload.y);
        }
    }

    @Override
    public void write(Writes write, BaseMount mount){
        super.write(write, mount);

        if(mount instanceof TurretMount m){
            write.f(m.charge);
        }
    }

    @Override
    public void read(Reads read, byte revision, BaseMount mount){
        super.read(read, revision, mount);

        if(mount instanceof TurretMount m){
            m.charge = read.f();
        }
    }
}
