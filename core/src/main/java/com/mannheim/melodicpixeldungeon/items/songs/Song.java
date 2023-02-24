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

import com.mannheim.melodicpixeldungeon.Dungeon;
import com.mannheim.melodicpixeldungeon.actors.Char;
import com.mannheim.melodicpixeldungeon.actors.buffs.Buff;
import com.mannheim.melodicpixeldungeon.actors.buffs.Invisibility;
import com.mannheim.melodicpixeldungeon.actors.buffs.MagicImmune;
import com.mannheim.melodicpixeldungeon.actors.hero.Hero;
import com.mannheim.melodicpixeldungeon.items.Generator;
import com.mannheim.melodicpixeldungeon.items.Item;
import com.mannheim.melodicpixeldungeon.items.ItemStatusHandler;
import com.mannheim.melodicpixeldungeon.items.artifacts.Artifact;
import com.mannheim.melodicpixeldungeon.items.artifacts.TalismanOfForesight;
import com.mannheim.melodicpixeldungeon.journal.Catalog;
import com.mannheim.melodicpixeldungeon.messages.Messages;
import com.mannheim.melodicpixeldungeon.sprites.HeroSprite;
import com.mannheim.melodicpixeldungeon.sprites.ItemSpriteSheet;
import com.mannheim.melodicpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

public abstract class Song extends Item {

	public int tier;

	//the current artifact charge
	protected int charge = 0;
	//the build towards next charge, usually rolls over at 1.
	//better to keep charge as an int and use a separate float than casting.
	protected float partialCharge = 0;
	//the maximum charge, varies per artifact, not all artifacts use this.
	protected int chargeCap = 10;

	public static final String AC_PLAY = "PLAY";

	protected static final float TIME_TO_PLAY = 1f;

	private static final LinkedHashMap<String, Integer> runes = new LinkedHashMap<String, Integer>() {
		{
			put("EIGHTH NOTES",ItemSpriteSheet.SONG);
			//put("SOWILO",ItemSpriteSheet.SCROLL_SOWILO);
			//put("LAGUZ",ItemSpriteSheet.SCROLL_LAGUZ);
			//put("YNGVI",ItemSpriteSheet.SCROLL_YNGVI);
			//put("GYFU",ItemSpriteSheet.SCROLL_GYFU);
			//put("RAIDO",ItemSpriteSheet.SCROLL_RAIDO);
			//put("ISAZ",ItemSpriteSheet.SCROLL_ISAZ);
			//put("MANNAZ",ItemSpriteSheet.SCROLL_MANNAZ);
			//put("NAUDIZ",ItemSpriteSheet.SCROLL_NAUDIZ);
			//put("BERKANAN",ItemSpriteSheet.SCROLL_BERKANAN);
			//put("ODAL",ItemSpriteSheet.SCROLL_ODAL);
			//put("TIWAZ",ItemSpriteSheet.SCROLL_TIWAZ);
		}
	};

	protected static ItemStatusHandler<Song> handler;

	protected String rune;

	{
		stackable = true;
		defaultAction = AC_PLAY;
	}

	@SuppressWarnings("unchecked")
	public static void initNames() {
		handler = new ItemStatusHandler<>( (Class<? extends Song>[])Generator.Category.SONG.classes, runes );
	}

	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}

	public static void saveSelectively( Bundle bundle, ArrayList<Item> items ) {
		ArrayList<Class<? extends Item>> classes = new ArrayList<>();
		for (Item i : items){
			if (i instanceof Song){
				if (!classes.contains(i.getClass())){
					classes.add(i.getClass());
				}
			}
		}
		handler.saveClassesSelectively( bundle, classes );
	}

	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>( (Class<? extends Song>[])Generator.Category.SONG.classes, runes, bundle );
	}

	public Song() {
		super();
		reset();
	}
	
	//anonymous songs are always IDed, do not affect ID status,
	//and their sprite is replaced by a placeholder if they are not known,
	//useful for items that appear in UIs, or which are only spawned for their effects
	//TODO take note of this when you implement songs!
	protected boolean anonymous = false;
	public void anonymize(){
		if (!isKnown()) image = ItemSpriteSheet.SCROLL_HOLDER;
		anonymous = true;
	}
	
	
	@Override
	public void reset(){
		super.reset();
		if (handler != null && handler.contains(this)) {
			image = handler.image(this);
			rune = handler.label(this);
		}
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add(AC_PLAY);
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_PLAY)) {
			
			if (hero.buff(MagicImmune.class) != null){
				GLog.w( Messages.get(this, "no_magic") );
			} else {
				curUser = hero;
				curItem = detach( hero.belongings.backpack );
				doPlay();
			}
			
		}
	}
	
	public abstract void doPlay();

	protected void readAnimation() {
		Invisibility.dispel();
		curUser.spend(TIME_TO_PLAY);
		curUser.busy();
		((HeroSprite)curUser.sprite).read();



	}
	
	public boolean isKnown() {
		return anonymous || (handler != null && handler.isKnown( this ));
	}
	
	public void setKnown() {
		if (!anonymous) {
			if (!isKnown()) {
				handler.know(this);
				updateQuickslot();
			}
			
			if (Dungeon.hero.isAlive()) {
				Catalog.setSeen(getClass());
			}
		}
	}
	
	@Override
	public Item identify( boolean byHero ) {
		super.identify(byHero);

		if (!isKnown()) {
			setKnown();
		}
		return this;
	}
	
	@Override
	public String name() {
		return isKnown() ? super.name() : Messages.get(this, rune);
	}
	
	@Override
	public String info() {
		return isKnown() ?
			desc() :
			Messages.get(this, "unknown_desc");
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return isKnown();
	}
	
	public static HashSet<Class<? extends Song>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Song>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler.known().size() == Generator.Category.SONG.classes.length;
	}
	
	@Override
	public int value() {
		return 30 * quantity;
	}

	@Override
	public int energyVal() {
		return 6 * quantity;
	}
	
	public static class PlaceHolder extends Song {
		
		{
			image = ItemSpriteSheet.SCROLL_HOLDER;
		}
		
		@Override
		public void doPlay() {}
		
		@Override
		public String info() {
			return "";
		}
	}

	//artifact-derived stuff
	@Override
	public String status() {

		//if the artifact isn't IDed, or is cursed, don't display anything
		if (!isIdentified() || cursed){
			return null;
		}

		//display as #/#
		if (chargeCap > 0)
			return Messages.format( "%d", chargeCap - charge );

		//otherwise, if there's no charge, return null.
		return null;
	}

	public class SongBuff extends Buff {

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				//if we're loading in and the hero has partially spent a turn, delay for 1 turn
				if (now() == 0 && cooldown() == 0 && target.cooldown() > 0) spend(TICK);
				return true;
			}
			return false;
		}

		public int itemLevel() {
			return level();
		}

		public boolean isCursed() {
			return cursed;
		}

		public void charge(Hero target, float amount){
			Song.this.charge(target, amount);
		}

	}

	public void charge(Hero target, float amount){
		if (cursed || target.buff(MagicImmune.class) != null) return;
		if (charge < chargeCap){
			charge += Math.round(2*amount);
			if (charge >= chargeCap) {
				charge = chargeCap;
				partialCharge = 0;
				GLog.p( Messages.get(TalismanOfForesight.class, "full_charge") );
			}
			updateQuickslot();
		}
	}

	//TODO ADD STORING IN BUNDLE STUFF FROM ARTIFACT.JAVA?
	//discord told me to add this?
	private static final String CHARGE = "charge";
	private static final String PARTIALCHARGE = "partialcharge";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( CHARGE , charge );
		bundle.put( PARTIALCHARGE , partialCharge );
	}


	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (chargeCap > 0)  charge = Math.min( chargeCap, bundle.getInt( CHARGE ));
		else                charge = bundle.getInt( CHARGE );
		partialCharge = bundle.getFloat( PARTIALCHARGE );
	}
}
