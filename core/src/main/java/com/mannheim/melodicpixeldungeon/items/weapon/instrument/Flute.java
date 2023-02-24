package com.mannheim.melodicpixeldungeon.items.weapon.instrument;

import com.mannheim.melodicpixeldungeon.Assets;
import com.mannheim.melodicpixeldungeon.actors.buffs.MagicImmune;
import com.mannheim.melodicpixeldungeon.actors.hero.Hero;
import com.mannheim.melodicpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.mannheim.melodicpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class Flute extends InstrumentWeapon {

    {
        image = ItemSpriteSheet.SHORTSWORD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.1f;

        tier = 1;
    }



    @Override
    public int max(int lvl) {
        return 3*(tier+1) +   //has 6 base max damage, down from 10
                lvl*(tier+1);
    }


}