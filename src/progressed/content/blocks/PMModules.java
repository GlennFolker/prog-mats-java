package progressed.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.bullet.physical.*;
import progressed.entities.bullet.physical.DelayBulletType.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.modular.modules.turret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.blocks.payloads.*;

import static mindustry.Vars.*;

public class PMModules implements ContentList{
    public static ModulePayload

    //Region Small

    shrapnel, froth, bifurcation, bandage, overclocker,

    //Region Medium

    blunderbuss, airburst, vulcan, lotus, gravity,

    //Region Large

    rebound, trifecta, jupiter, diffusion;

    @Override
    public void load(){
        //Region small
        shrapnel = new ModulePayload("shrapnel"){{
            module = new ItemTurretModule("shrapnel"){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopper,
                    Items.graphite, ModuleBullets.shotgunDense,
                    Items.titanium, ModuleBullets.shotgunTitanium,
                    Items.thorium, ModuleBullets.shotgunThorium
                );

                reloadTime = 90f;
                shootCone = 30;
                range = 120f;
                maxAmmo = 10;
                shots = 5;
                inaccuracy = 25;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 9f;

                limitRange(6f);
            }};
        }};

        froth = new ModulePayload("froth"){{
            module = new LiquidTurretModule("froth"){{
                ammo(
                    Liquids.water, ModuleBullets.waterShotMini,
                    Liquids.slag, ModuleBullets.slagShotMini,
                    Liquids.cryofluid, ModuleBullets.cryoShotMini,
                    Liquids.oil, ModuleBullets.oilShotMini
                );

                reloadTime = 5f;
                range = 96f;
                targetAir = false;
                inaccuracy = 8;
                recoilAmount = 0f;
                shootCone = 20f;
                shootEffect = Fx.shootLiquid;
            }};
        }};

        bifurcation = new ModulePayload("bifurcation"){{
            module = new PowerTurretModule("bifurcation"){{
                shootType = new LightningBulletType(){{
                    damage = 12;
                    lightningLength = 20;
                    collidesAir = false;
                    displayAmmoMultiplier = false;
                }};

                reloadTime = 35f;
                shootCone = 40f;
                powerUse = 3.5f;
                targetAir = false;
                range = 100f;
                barrels = shots = 2;
                barrelSpacing = 3;
                shootEffect = Fx.lightningShoot;
                heatColor = Color.red;
                shootSound = Sounds.spark;
            }};
        }};

        //amazing name lmao
        bandage = new ModulePayload("bandage"){{
            module = new ChargeModule("bandage"){
                final float healPercent = 5f;

                {
                    powerUse = 0.75f;
                    reload = 150f;

                    activate = (p, m) -> {
                        if(p.damaged()){
                            p.heal(p.maxHealth() * healPercent / 100f * efficiency(p));
                            Fx.healBlockFull.at(p.x, p.y, p.block.size, PMPal.heal);
                        }
                    };

                    setStats = () -> stats.add(Stat.repairTime, (int)(100f / healPercent * reload / 60f), StatUnit.seconds);
                }

                @Override
                public boolean isActive(ModularTurretBuild parent, BaseMount mount){
                    return super.isActive(parent, mount) && parent.damaged();
                }
            };
        }};

        overclocker = new ModulePayload("mini-overdrive"){{
            module = new ChargeModule("mini-overdrive"){
                final float speedBoost = 1.25f;

                {
                    powerUse = 0.75f;
                    single = true;

                    activate = (p, m) -> {
                        m.charge = 0f;
                        p.applyBoost(speedBoost * p.efficiency(), 61f);
                    };

                    setStats = () -> stats.add(Stat.speedIncrease, "+" + (int)(speedBoost * 100f - 100) + "%");
                }
            };
        }};
        //endregion

        //Region medium
        blunderbuss = new ModulePayload("blunderbuss"){{
            size = 2;

            module = new ItemTurretModule("blunderbuss", ModuleSize.medium){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopperCrit,
                    Items.graphite, ModuleBullets.shotgunDenseCrit,
                    Items.titanium, ModuleBullets.shotgunTitaniumCrit,
                    Items.thorium, ModuleBullets.shotgunThoriumCrit
                );

                reloadTime = 75f;
                shootCone = 25;
                range = 200f;
                maxAmmo = 10;
                shots = 6;
                inaccuracy = 20;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 9f;
                shootSound = Sounds.shootBig;

                limitRange(5f);
            }};
        }};

        airburst = new ModulePayload("airburst"){{
            size = 2;

            module = new ItemTurretModule("airburst", ModuleSize.medium){{
                ammo(
                    Items.pyratite, ModuleBullets.swarmIncendiary,
                    Items.blastCompound, ModuleBullets.swarmBlast
                );

                reloadTime = 60f;
                range = 200f;
                shootCone = 45;
                shots = 7;
                xRand = 4f;
                burstSpacing = 2f;
                inaccuracy = 7f;
                velocityInaccuracy = 0.2f;
                rotateSpeed = 3f;
                shootSound = Sounds.missile;
            }};
        }};

        vulcan = new ModulePayload("vulcan"){{
            size = 2;

            module = new PowerTurretModule("vulcan", ModuleSize.medium){{
                reloadTime = 43f;
                powerUse = 10f;
                range = 116;
                rotateSpeed = 6.5f;
                shootSound = Sounds.shotgun;
                heatColor = Color.red;

                float brange = range + 10f;

                shootType = new ShrapnelBulletType(){{
                    damage = 67f;
                    length = brange;
                    width = 12f;
                    toColor = Pal.remove;
                    displayAmmoMultiplier = false;
                    shootEffect = smokeEffect = ModuleFx.flameShoot;
                    makeFire = true;
                    status = StatusEffects.burning;
                }};
            }};
        }};

        lotus = new ModulePayload("iris"){{
            size = 2;

            module = new PowerTurretModule("iris", ModuleSize.medium){
                final float delay = 30f;
                final Effect waveEffect = Fx.none;

                {
                    reloadTime = 60f;
                    shots = 8;
                    burstSpacing = 2f;
                    minRange = 4f * 8f;
                    range = 28f * 8f;
                    shootEffect = ModuleFx.lotusShoot;
                    smokeEffect = ModuleFx.lotusShootSmoke;
                    powerUse = 12f;
                    drawRotate = false;

                    shootType = new DelayBulletType(5f, 36f, "prog-mats-lance"){{
                        frontColor = Color.white;
                        backColor = trailColor = Pal.surge;
                        width = height = 8f;
                        shrinkX = shrinkY = 0;
                        lifetime = 60f;
                        drag = 0.15f;
                        homingPower = 0.15f;
                        trailLength = 5;
                        trailWidth = 1f;
                        hitEffect = despawnEffect = ModuleFx.hitLotus;
                    }};
                }

                @Override
                public void turnToTarget(ModularTurretBuild parent, TurretMount mount, float targetRot){
                    mount.rotation = targetRot;
                }

                @Override
                public void shoot(ModularTurretBuild parent, TurretMount mount, BulletType type){
                    float x = mount.x,
                        y = mount.y;

                    tr.set(mount.targetPos).sub(mount.x, mount.y);
                    if(tr.len() < minRange) tr.setLength(minRange);
                    tr.add(mount.x, mount.y);

                    float aimX = tr.x,
                        aimY = tr.y;

                    for(int i = 0; i < shots; i++){
                        mount.isShooting = true;
                        float rot = 90f - 360f / shots * i;
                        int ii = i;
                        Time.run(burstSpacing * i, () -> {
                            mount.isShooting = true;
                            if(!mount.valid(parent)){
                                mount.isShooting = false;
                                return;
                            }

                            tr.trns(rot, shootLength);
                            type.create(parent, parent.team, x + tr.x, y + tr.y, rot, -1, 1f + Mathf.range(velocityInaccuracy), 1f, new DelayBulletData(aimX, aimY, delay - ii * burstSpacing));

                            Effect fshootEffect = shootEffect == Fx.none ? type.shootEffect : shootEffect;
                            Effect fsmokeEffect = smokeEffect == Fx.none ? type.smokeEffect : smokeEffect;

                            fshootEffect.at(x + tr.x, y + tr.y, rot);
                            fsmokeEffect.at(x + tr.x, y + tr.y, rot);
                            shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

                            if(shootShake > 0){
                                Effect.shake(shootShake, shootShake, x, y);
                            }

                            mount.heat = 1f;

                            if(ii == shots - 1) mount.isShooting = false;
                        });
                    }

                    Time.run(delay, () -> {
                        if(mount.valid(parent)) waveEffect.at(x, y);
                    });
                }
            };
        }};

        gravity = new ModulePayload("gravity"){{
            size = 2;

            module = new ImpulseModule("gravity", ModuleSize.medium){{
                range = 180f;
                damage = 0.2f;
                powerUse = 2f;
                radius = 5f;
                laserWidth = 0.3f;
                maxTargets = 6;
                force = 30f;
                scaledForce = 22f;
            }};
        }};
        //endregion

        //Region Large
        rebound = new ModulePayload("rebound"){{
            size = 3;

            module = new ItemTurretModule("rebound", ModuleSize.large){
                {
                    ammo(
                        Items.titanium, ModuleBullets.reboundTitanium,
                        Items.surgeAlloy, ModuleBullets.reboundSurge
                    );

                    range = 21f * tilesize;
                    reloadTime = 75f;
                    shootLength = 1f / 4f;
                    recoilAmount = 1;
                    topLayerOffset = 0.3f;
                    shootEffect = ModuleFx.reboundShoot;
                }

                @Override
                public void handleItem(Item item, BaseMount mount){
                    BulletType type = ammoTypes.get(item);
                    if(type != null) ((TurretMount)mount).reload = 0f;

                    super.handleItem(item, mount);
                }

                @Override
                protected void effects(TurretMount mount, BulletType type){
                    float x = mount.x, y = mount.y;
                    BoomerangBulletType b = (BoomerangBulletType)type;

                    Effect fshootEffect = shootEffect == Fx.none ? type.shootEffect : shootEffect;
                    Effect fsmokeEffect = smokeEffect == Fx.none ? type.smokeEffect : smokeEffect;

                    fshootEffect.at(x + tr.x, y + tr.y, b.width / 2f, b.backColor);
                    fsmokeEffect.at(x + tr.x, y + tr.y, b.width / 2f, b.backColor);
                    shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));

                    if(shootShake > 0){
                        Effect.shake(shootShake, shootShake, x, y);
                    }

                    mount.recoil = recoilAmount;
                }

                @Override
                public void drawTurret(ModularTurretBuild parent, TurretMount mount){
                    super.drawTurret(parent, mount);

                    if(hasAmmo(mount)){
                        BoomerangBulletType b = (BoomerangBulletType)peekAmmo(mount);
                        float r = mount.reload / reloadTime,
                            width = b.width * r,
                            height = b.height * r,
                            spin = mount.rotation + Time.time * b.spin;
                        tr.trns(mount.rotation, -mount.recoil + shootLength);
                        Draw.z(b.layer);
                        Draw.color(b.backColor);
                        Draw.rect(b.backRegion, mount.x + tr.x, mount.y + tr.y, width, height, spin);
                        Draw.color(b.frontColor);
                        Draw.rect(b.frontRegion, mount.x + tr.x, mount.y + tr.y, width, height, spin);
                    }
                }
            };
        }};

        trifecta = new ModulePayload("trifecta"){{
            size = 3;

            module = new ItemTurretModule("trifecta", ModuleSize.large){
                {
                    ammo(
                        Items.blastCompound, ModuleBullets.trifectaMissile
                    );

                    range = 34f * tilesize;
                    reloadTime = 120f;
                    maxAmmo = 12;
                    shots = 3;
                    barrels = 3;
                    barrelSpacing = 6f;
                    rotateShooting = false;
                    burstSpacing = 15f;
                    shootLength -= 6f;
                    topLayerOffset = 0.30f;
                    shootSound = Sounds.artillery;
                }

                @Override
                public void createIcons(MultiPacker packer){
                    Outliner.outlineRegion(
                        packer,
                        Core.atlas.find("prog-mats-trifecta-missile"),
                        outlineColor,
                        "prog-mats-trifecta-missile-outline"
                    );
                    super.createIcons(packer);
                }
            };
        }};

        jupiter = new ModulePayload("jupiter"){{
            size = 3;

            module = new PowerTurretModule("jupiter", ModuleSize.large){
                final float baseRad = 7.5f, jointRadMin = 9.5f, jointRadMax = 13f, endRadMin = 5f, endRadMax = 11.5f;
                TextureRegion jointBaseRegion, armBaseRegion, jointRegion, armRegion, endRegion, fullArm;

                {
                    reloadTime = 2.5f * 60f;
                    range = 24f * 8f;
                    powerUse = 4f;
                    recoilAmount = shootLength = 0;
                    chargeTime = ModuleFx.jupiterCharge.lifetime;
                    chargeBeginEffect = ModuleFx.jupiterCharge;
                    shootSound = Sounds.laser;

                    shootType = ModuleBullets.jupiterOrb;
                }

                @Override
                public void load(){
                    super.load();
                    jointBaseRegion = Core.atlas.find(name + "-joint-base");
                    armBaseRegion = Core.atlas.find(name + "-arm-base");
                    jointRegion = Core.atlas.find(name + "-joint");
                    armRegion = Core.atlas.find(name + "-arm");
                    endRegion = Core.atlas.find(name + "-end");
                    fullArm = Core.atlas.find(name + "-arm-full");
                }

                @Override
                public void updateCharging(ModularTurretBuild parent, TurretMount mount){
                    if(!mount.charging){
                        mount.charge = Mathf.approachDelta(mount.charge, 0f, 3f);
                    }else{
                        mount.charge = Mathf.approachDelta(mount.charge, chargeTime, 1f);

                        if(mount.charge >= chargeTime){
                            mount.charging = false;
                            chargeShot(parent, mount);
                        }
                    }
                }

                @Override
                public void turnToTarget(ModularTurretBuild parent, TurretMount mount, float targetRot){
                    mount.rotation = targetRot;
                }

                @Override
                public boolean shouldTurn(TurretMount mount){
                    return true;
                }

                @Override
                public boolean shouldReload(ModularTurretBuild parent, TurretMount mount){
                    return super.shouldReload(parent, mount) && mount.charge == 0;
                }

                @Override
                public void drawPayload(ModulePayloadBuild payload){
                    float x = payload.x,
                        y = payload.y;

                    Draw.rect(region, x, y);
                    for(int i = 0; i < 4; i++){
                        float rot = i * 90 - 45f;
                        Draw.rect(fullArm, x, y, rot);
                    }
                }

                @Override
                public void drawTurret(ModularTurretBuild parent, TurretMount mount){
                    float x = mount.x,
                        y = mount.y;

                    if(mount.progress < deployTime){
                        Draw.draw(Draw.z(), () -> {
                            float progress = mount.progress / deployTime;
                            PMDrawf.blockBuildCenter(x, y, region, 0, progress);
                            for(int i = 0; i < 4; i++){
                                float rot = i * 90 - 45f;
                                PMDrawf.blockBuildCenter(x, y, fullArm, rot, progress);
                            }
                        });
                        return;
                    }

                    applyColor(parent, mount);
                    Draw.rect(region, x, y);

                    Lines.stroke(6f);
                    float charge = Interp.pow2Out.apply(mount.charge / chargeTime);
                    for(int i = 0; i < 4; i++){
                        float rot = i * 90 - 45f,
                            joint = Mathf.lerp(jointRadMin, jointRadMax, charge),
                            end = Mathf.lerp(endRadMin, endRadMax, charge);

                        tr.trns(rot, baseRad);
                        tr2.trns(rot, joint);

                        Draw.rect(jointBaseRegion, x + tr.x, y + tr.y, rot);
                        PMDrawf.stretch(armBaseRegion, x + tr.x, y + tr.y, x + tr2.x, y + tr2.y);
                        Draw.rect(jointRegion, x + tr2.x, y + tr2.y, rot);

                        tr.trns(rot, end);

                        PMDrawf.stretch(armRegion, x + tr2.x, y + tr2.y, x + tr.x, y + tr.y);
                        Draw.rect(endRegion, x + tr.x, y + tr.y, rot);
                    }
                    Draw.mixcol();
                }
            };
        }};

        diffusion = new ModulePayload("diffusion"){{
            size = 3;

            module = new ChargeModule("diffusion", ModuleSize.large){
                final float radius = 32f * tilesize;
                final float damage = 0.25f, scaledDamage = 5.5f;

                {
                    reload = 1f;
                    powerUse = 17f;

                    activate = (p, m) -> {
                        Groups.bullet.intersect(m.x - radius, m.y - radius, radius * 2f, radius * 2f, b -> {
                            if(b.type.hittable && b.team != p.team && b.within(m, radius)){
                                float scl = 1f - m.dst(b) / radius;
                                float d = (damage + scaledDamage * scl) * efficiency(p);

                                if(b.damage() > d){
                                    b.damage(b.damage() - d);;
                                }else{
                                    b.remove();
                                }

                                ModuleFx.diffusionDamage.at(b.x, b.y, b.hitSize * 3f * (0.1f + scl * 0.9f), p.team.color);
                            }
                        });
                    };
                }

                @Override
                public void setStats(Stats stats){
                    super.setStats(stats);

                    stats.add(Stat.range, radius / Vars.tilesize, StatUnit.blocks);
                }

                @Override
                public void draw(ModularTurretBuild parent, BaseMount mount){
                    super.draw(parent, mount);

                    ChargeMount m = (ChargeMount)mount;
                    if(m.smoothEfficiency > 0.001f){
                        Draw.z(Layer.shields - 0.99f);
                        Draw.color(parent.team.color, 0.5f * m.smoothEfficiency);
                        Lines.stroke(2f * m.smoothEfficiency);
                        Lines.circle(mount.x, mount.y, radius - Lines.getStroke() / 2f);
                    }
                }

                @Override
                public void drawHighlight(ModularTurretBuild parent, BaseMount mount){
                    Drawf.dashCircle(mount.x, mount.y, radius, parent.team.color);
                }
            };
        }};
        //endregion
    }
}