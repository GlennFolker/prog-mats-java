package progressed.entities.bullet;

import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import progressed.entities.bullet.SnakeBulletType.SnakeBulletData.*;

public class SnakeBulletType extends BasicBulletType{
    public int length = 3;
    public float spawnDelay= 5f;
    public float followPower = 0.85f;
    public boolean setSegments = true;

    public SnakeBulletType head;
    public SnakeBulletType body;
    public SnakeBulletType tail;

    public SnakeBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage, bulletSprite);
        backMove = false;
    }

    @Override
    public void init(){
        super.init();

        if(setSegments){
            if(body == null){
                body = (SnakeBulletType)copy();
                body.setSegments = false;
                body.backMove = true;
            }
            if(head == null){
                head = (SnakeBulletType)body.copy();
                head.sprite += "-head";
                head.setSegments = false;
                head.backMove = true;
            }
            if(tail == null){
                tail = (SnakeBulletType)body.copy();
                tail.sprite += "-tail";
                head.setSegments = false;
                head.backMove = true;
            }
        }

        length = Math.max(length, 3);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(b.data == null){
            Bullet[] next = {null};
            float x = b.x;
            float y = b.y;
            for(int i = 0; i < length; i++){
                int ii = i;

                Time.run(i * spawnDelay, () -> {
                    SnakeBulletType bType = body;
                    SegmentType sType = SegmentType.body;
                    if(ii == 0){
                        bType = head;
                        sType = SegmentType.head;
                    }else if(ii == length - 1){
                        bType = tail;
                        sType = SegmentType.tail;
                    }

                    Bullet seg = bType.create(
                        b.owner, b.team,
                        x, y, next[0] != null ? next[0].angleTo(x, y) + 180f : b.rotation(),
                        -1f, 1f, 1f,
                        new SnakeBulletData(sType, next[0])
                    );
                    next[0] = seg;
                });
            }

            b.time = b.lifetime;
            b.remove();
        }
    }

    @Override
    public void despawned(Bullet b){
        if(b.data == null) return;
        super.despawned(b);
    }

    public void update(Bullet b){
        updateTrail(b);

        SnakeBulletData data = (SnakeBulletData)b.data;
        if(data == null) return;
        if(data.segmentType == SegmentType.head){
            updateHoming(b);
            updateWeaving(b);
        }else if(data.followBullet != null){
            if(data.followBullet.isAdded()){
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(data.followBullet), followPower * Time.delta * 50f));
            }else{
                data.followBullet = null;
            }
        }

        updateTrailEffects(b);
        updateBulletInterval(b);
    }

    public static class SnakeBulletData{
        public SegmentType segmentType;
        public Bullet followBullet;

        public SnakeBulletData(SegmentType type, Bullet b){
            segmentType = type;
            followBullet = b;
        }

        public enum SegmentType{
            head, body, tail
        }
    }
}
