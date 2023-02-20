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

package com.mannheim.melodicpixeldungeon.items.armor.glyphs;

import com.mannheim.melodicpixeldungeon.actors.Char;
import com.mannheim.melodicpixeldungeon.actors.buffs.Charm;
import com.mannheim.melodicpixeldungeon.actors.buffs.Degrade;
import com.mannheim.melodicpixeldungeon.actors.buffs.Hex;
import com.mannheim.melodicpixeldungeon.actors.buffs.MagicalSleep;
import com.mannheim.melodicpixeldungeon.actors.buffs.Vulnerable;
import com.mannheim.melodicpixeldungeon.actors.buffs.Weakness;
import com.mannheim.melodicpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.mannheim.melodicpixeldungeon.actors.mobs.DM100;
import com.mannheim.melodicpixeldungeon.actors.mobs.Eye;
import com.mannheim.melodicpixeldungeon.actors.mobs.Shaman;
import com.mannheim.melodicpixeldungeon.actors.mobs.Warlock;
import com.mannheim.melodicpixeldungeon.actors.mobs.YogFist;
import com.mannheim.melodicpixeldungeon.items.armor.Armor;
import com.mannheim.melodicpixeldungeon.items.bombs.Bomb;
import com.mannheim.melodicpixeldungeon.items.rings.RingOfArcana;
import com.mannheim.melodicpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.mannheim.melodicpixeldungeon.items.wands.CursedWand;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfBlastWave;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfDisintegration;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfFireblast;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfFrost;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfLightning;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfLivingEarth;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfMagicMissile;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfPrismaticLight;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfTransfusion;
import com.mannheim.melodicpixeldungeon.items.wands.WandOfWarding;
import com.mannheim.melodicpixeldungeon.levels.traps.DisintegrationTrap;
import com.mannheim.melodicpixeldungeon.levels.traps.GrimTrap;
import com.mannheim.melodicpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class AntiMagic extends Armor.Glyph {

	private static ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x88EEFF );
	
	public static final HashSet<Class> RESISTS = new HashSet<>();
	static {
		RESISTS.add( MagicalSleep.class );
		RESISTS.add( Charm.class );
		RESISTS.add( Weakness.class );
		RESISTS.add( Vulnerable.class );
		RESISTS.add( Hex.class );
		RESISTS.add( Degrade.class );
		
		RESISTS.add( DisintegrationTrap.class );
		RESISTS.add( GrimTrap.class );

		RESISTS.add( Bomb.MagicalBomb.class );
		RESISTS.add( ScrollOfPsionicBlast.class );

		RESISTS.add( CursedWand.class );
		RESISTS.add( WandOfBlastWave.class );
		RESISTS.add( WandOfDisintegration.class );
		RESISTS.add( WandOfFireblast.class );
		RESISTS.add( WandOfFrost.class );
		RESISTS.add( WandOfLightning.class );
		RESISTS.add( WandOfLivingEarth.class );
		RESISTS.add( WandOfMagicMissile.class );
		RESISTS.add( WandOfPrismaticLight.class );
		RESISTS.add( WandOfTransfusion.class );
		RESISTS.add( WandOfWarding.Ward.class );

		RESISTS.add( WarpBeacon.class );
		
		RESISTS.add( DM100.LightningBolt.class );
		RESISTS.add( Shaman.EarthenBolt.class );
		RESISTS.add( Warlock.DarkBolt.class );
		RESISTS.add( Eye.DeathGaze.class );
		RESISTS.add( YogFist.BrightFist.LightBeam.class );
		RESISTS.add( YogFist.DarkFist.DarkBolt.class );
	}
	
	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {
		//no proc effect, see:
		// Hero.damage
		// GhostHero.damage
		// Shadowclone.damage
		// ArmoredStatue.damage
		// PrismaticImage.damage
		return damage;
	}
	
	public static int drRoll( Char ch, int level ){
		return Random.NormalIntRange(
				Math.round(level * RingOfArcana.enchantPowerMultiplier(ch)),
				Math.round((3 + (level*1.5f)) * RingOfArcana.enchantPowerMultiplier(ch)));
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return TEAL;
	}

}