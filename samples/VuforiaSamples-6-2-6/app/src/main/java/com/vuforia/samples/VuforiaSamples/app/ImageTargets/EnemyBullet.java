package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

/**
 * Created by Administrator on 2017/1/6.
 */

public class EnemyBullet extends Bullet{
   public EnemyBullet(Craft belong){
       super(belong);
       bulletType = 1;
   }

}
