/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.mannheim.melodicpixeldungeon.items.songs;

import com.mannheim.melodicpixeldungeon.Assets;
import com.mannheim.melodicpixeldungeon.Dungeon;
import com.mannheim.melodicpixeldungeon.actors.buffs.Buff;
import com.mannheim.melodicpixeldungeon.actors.buffs.Drowsy;
import com.mannheim.melodicpixeldungeon.actors.mobs.Mob;
import com.mannheim.melodicpixeldungeon.effects.Speck;
import com.mannheim.melodicpixeldungeon.messages.Messages;
import com.mannheim.melodicpixeldungeon.sprites.ItemSpriteSheet;
import com.mannheim.melodicpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class SongOfLullaby extends Song {

	{
		//icon = the small image in the top right corner
		icon = ItemSpriteSheet.Icons.SCROLL_LULLABY;
		tier = 1;
	}

	@Override
	public void doPlay() {
		collect();
		curUser.sprite.centerEmitter().start( Speck.factory( Speck.NOTE ), 0.3f, 5 );
		Sample.INSTANCE.play( Assets.Sounds.LULLABY );

		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Dungeon.level.heroFOV[mob.pos]) {
				Buff.affect( mob, Drowsy.class );
				mob.sprite.centerEmitter().start( Speck.factory( Speck.NOTE ), 0.3f, 5 );
			}
		}

		Buff.affect( curUser, Drowsy.class );

		GLog.i( Messages.get(this, "sooth") );

		identify();
		readAnimation();
		//collect();
	}
	
	@Override
	public int value() {
		return isKnown() ? 40 * quantity : super.value();
	}

	//discord told me to add this?
	private static final String TIER = "tier";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TIER, tier);
	}


	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		tier = bundle.getInt(TIER);
	}
}
