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

package com.mannheim.melodicpixeldungeon.items;

import com.mannheim.melodicpixeldungeon.Assets;
import com.mannheim.melodicpixeldungeon.Dungeon;
import com.mannheim.melodicpixeldungeon.actors.hero.Hero;
import com.mannheim.melodicpixeldungeon.actors.mobs.Wraith;
import com.mannheim.melodicpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.mannheim.melodicpixeldungeon.effects.CellEmitter;
import com.mannheim.melodicpixeldungeon.effects.Speck;
import com.mannheim.melodicpixeldungeon.effects.particles.ElmoParticle;
import com.mannheim.melodicpixeldungeon.effects.particles.ShadowParticle;
import com.mannheim.melodicpixeldungeon.items.armor.Armor;
import com.mannheim.melodicpixeldungeon.items.artifacts.Artifact;
import com.mannheim.melodicpixeldungeon.items.bombs.Bomb;
import com.mannheim.melodicpixeldungeon.items.food.ChargrilledMeat;
import com.mannheim.melodicpixeldungeon.items.food.FrozenCarpaccio;
import com.mannheim.melodicpixeldungeon.items.food.MysteryMeat;
import com.mannheim.melodicpixeldungeon.items.journal.DocumentPage;
import com.mannheim.melodicpixeldungeon.items.journal.Guidebook;
import com.mannheim.melodicpixeldungeon.items.potions.Potion;
import com.mannheim.melodicpixeldungeon.items.rings.RingOfWealth;
import com.mannheim.melodicpixeldungeon.items.scrolls.Scroll;
import com.mannheim.melodicpixeldungeon.items.wands.Wand;
import com.mannheim.melodicpixeldungeon.journal.Document;
import com.mannheim.melodicpixeldungeon.messages.Messages;
import com.mannheim.melodicpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class Heap implements Bundlable {
	
	public enum Type {
		HEAP,
		FOR_SALE,
		CHEST,
		LOCKED_CHEST,
		CRYSTAL_CHEST,
		TOMB,
		SKELETON,
		REMAINS
	}
	public Type type = Type.HEAP;
	
	public int pos = 0;
	
	public ItemSprite sprite;
	public boolean seen = false;
	public boolean haunted = false;
	public boolean autoExplored = false; //used to determine if this heap should count for exploration bonus
	
	public LinkedList<Item> items = new LinkedList<>();
	
	public void open( Hero hero ) {
		switch (type) {
		case TOMB:
			Wraith.spawnAround( hero.pos );
			break;
		case REMAINS:
		case SKELETON:
			CellEmitter.center( pos ).start(Speck.factory(Speck.RATTLE), 0.1f, 3);
			break;
		default:
		}
		
		if (haunted){
			if (Wraith.spawnAt( pos ) == null) {
				hero.sprite.emitter().burst( ShadowParticle.CURSE, 6 );
				hero.damage( hero.HP / 2, this );
			}
			Sample.INSTANCE.play( Assets.Sounds.CURSED );
		}

		type = Type.HEAP;
		ArrayList<Item> bonus = RingOfWealth.tryForBonusDrop(hero, 1);
		if (bonus != null && !bonus.isEmpty()) {
			items.addAll(0, bonus);
			RingOfWealth.showFlareForBonusDrop(sprite);
		}
		sprite.link();
		sprite.drop();
	}
	
	public Heap setHauntedIfCursed(){
		for (Item item : items) {
			if (item.cursed) {
				haunted = true;
				item.cursedKnown = true;
				break;
			}
		}
		return this;
	}
	
	public int size() {
		return items.size();
	}
	
	public Item pickUp() {
		
		if (items.isEmpty()){
			destroy();
			return null;
		}
		Item item = items.removeFirst();
		if (items.isEmpty()) {
			destroy();
		} else if (sprite != null) {
			sprite.view(this).place( pos );
		}
		
		return item;
	}
	
	public Item peek() {
		return items.peek();
	}
	
	public void drop( Item item ) {
		
		if (item.stackable && type != Type.FOR_SALE) {
			
			for (Item i : items) {
				if (i.isSimilar( item )) {
					item = i.merge( item );
					break;
				}
			}
			items.remove( item );
			
		}

		//lost backpack must always be on top of a heap
		if ((item.dropsDownHeap && type != Type.FOR_SALE) || peek() instanceof LostBackpack) {
			items.add( item );
		} else {
			items.addFirst( item );
		}
		
		if (sprite != null) {
			sprite.view(this).place( pos );
		}
	}
	
	public void replace( Item a, Item b ) {
		int index = items.indexOf( a );
		if (index != -1) {
			items.remove( index );
			for (Item i : items) {
				if (i.isSimilar( b )) {
					i.merge( b );
					return;
				}
			}
			items.add( index, b );
		}
	}
	
	public void remove( Item a ){
		items.remove(a);
		if (items.isEmpty()){
			destroy();
		} else if (sprite != null) {
			sprite.view(this).place( pos );
		}
	}
	
	public void burn() {

		if (type != Type.HEAP) {
			return;
		}
		
		boolean burnt = false;
		boolean evaporated = false;
		
		for (Item item : items.toArray( new Item[0] )) {
			if (item instanceof Scroll && !item.unique) {
				items.remove( item );
				burnt = true;
			} else if (item instanceof Dewdrop) {
				items.remove( item );
				evaporated = true;
			} else if (item instanceof MysteryMeat || item instanceof FrozenCarpaccio) {
				replace( item, ChargrilledMeat.cook( item.quantity ) );
				burnt = true;
			} else if (item instanceof Bomb) {
				items.remove( item );
				((Bomb) item).explode( pos );
				if (((Bomb) item).explodesDestructively()) {
					//stop processing the burning, it will be replaced by the explosion.
					return;
				} else {
					burnt = true;
				}
			}
		}
		
		if (burnt || evaporated) {
			
			if (Dungeon.level.heroFOV[pos]) {
				if (burnt) {
					burnFX( pos );
				} else {
					evaporateFX( pos );
				}
			}
			
			if (isEmpty()) {
				destroy();
			} else if (sprite != null) {
				sprite.view(this).place( pos );
			}
			
		}
	}

	//Note: should not be called to initiate an explosion, but rather by an explosion that is happening.
	public void explode() {

		//breaks open most standard containers, mimics die.
		if (type == Type.CHEST || type == Type.SKELETON) {
			type = Type.HEAP;
			sprite.link();
			sprite.drop();
			return;
		}

		if (type != Type.HEAP) {

			return;

		} else {

			for (Item item : items.toArray( new Item[0] )) {

				//unique items aren't affect by explosions
				if (item.unique || (item instanceof Armor && ((Armor) item).checkSeal() != null)){
					continue;
				}

				if (item instanceof Potion) {
					items.remove(item);
					((Potion) item).shatter(pos);

				} else if (item instanceof Honeypot.ShatteredPot) {
					items.remove(item);
					((Honeypot.ShatteredPot) item).destroyPot(pos);

				} else if (item instanceof Bomb) {
					items.remove( item );
					((Bomb) item).explode(pos);
					if (((Bomb) item).explodesDestructively()) {
						//stop processing current explosion, it will be replaced by the new one.
						return;
					}

				//upgraded items can endure the blast
				} else if (item.level() <= 0) {
					items.remove( item );
				}

			}

			if (isEmpty()){
				destroy();
			} else if (sprite != null) {
				sprite.view(this).place( pos );
			}
		}
	}
	
	public void freeze() {

		if (type != Type.HEAP) {
			return;
		}
		
		boolean frozen = false;
		for (Item item : items.toArray( new Item[0] )) {
			if (item instanceof MysteryMeat) {
				replace( item, FrozenCarpaccio.cook( (MysteryMeat)item ) );
				frozen = true;
			} else if (item instanceof Potion && !item.unique) {
				items.remove(item);
				((Potion) item).shatter(pos);
				frozen = true;
			} else if (item instanceof Bomb){
				((Bomb) item).fuse = null;
				frozen = true;
			}
		}
		
		if (frozen) {
			if (isEmpty()) {
				destroy();
			} else if (sprite != null) {
				sprite.view(this).place( pos );
			}
		}
	}
	
	public static void burnFX( int pos ) {
		CellEmitter.get( pos ).burst( ElmoParticle.FACTORY, 6 );
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
	}
	
	public static void evaporateFX( int pos ) {
		CellEmitter.get( pos ).burst( Speck.factory( Speck.STEAM ), 5 );
	}
	
	public boolean isEmpty() {
		return items == null || items.size() == 0;
	}
	
	public void destroy() {
		Dungeon.level.heaps.remove( this.pos );
		if (sprite != null) {
			sprite.kill();
		}
		items.clear();
	}

	public String title(){
		switch(type){
			case FOR_SALE:
				Item i = peek();
				if (size() == 1) {
					return Messages.get(this, "for_sale", Shopkeeper.sellPrice(i), i.title());
				} else {
					return i.title();
				}
			case CHEST:
				return Messages.get(this, "chest");
			case LOCKED_CHEST:
				return Messages.get(this, "locked_chest");
			case CRYSTAL_CHEST:
				return Messages.get(this, "crystal_chest");
			case TOMB:
				return Messages.get(this, "tomb");
			case SKELETON:
				return Messages.get(this, "skeleton");
			case REMAINS:
				return Messages.get(this, "remains");
			default:
				return peek().title();
		}
	}

	public String info(){
		switch(type){
			case CHEST:
				return Messages.get(this, "chest_desc");
			case LOCKED_CHEST:
				return Messages.get(this, "locked_chest_desc");
			case CRYSTAL_CHEST:
				if (peek() instanceof Artifact)
					return Messages.get(this, "crystal_chest_desc", Messages.get(this, "artifact") );
				else if (peek() instanceof Wand)
					return Messages.get(this, "crystal_chest_desc", Messages.get(this, "wand") );
				else
					return Messages.get(this, "crystal_chest_desc", Messages.get(this, "ring") );
			case TOMB:
				return Messages.get(this, "tomb_desc");
			case SKELETON:
				return Messages.get(this, "skeleton_desc");
			case REMAINS:
				return Messages.get(this, "remains_desc");
			default:
				return peek().info();
		}
	}

	private static final String POS		= "pos";
	private static final String SEEN	= "seen";
	private static final String TYPE	= "type";
	private static final String ITEMS	= "items";
	private static final String HAUNTED	= "haunted";
	private static final String AUTO_EXPLORED	= "auto_explored";
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		pos = bundle.getInt( POS );
		seen = bundle.getBoolean( SEEN );
		type = Type.valueOf( bundle.getString( TYPE ) );
		
		items = new LinkedList<>((Collection<Item>) ((Collection<?>) bundle.getCollection(ITEMS)));
		items.removeAll(Collections.singleton(null));
		
		//remove any document pages that either don't exist anymore or that the player already has
		for (Item item : items.toArray(new Item[0])){
			if (item instanceof DocumentPage
					&& ( !((DocumentPage) item).document().pageNames().contains(((DocumentPage) item).page())
					||    ((DocumentPage) item).document().isPageFound(((DocumentPage) item).page()))){
				items.remove(item);
			}
			if (item instanceof Guidebook && Document.ADVENTURERS_GUIDE.isPageRead(0)){
				items.remove(item);
			}
		}
		
		haunted = bundle.getBoolean( HAUNTED );
		autoExplored = bundle.getBoolean( AUTO_EXPLORED );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( POS, pos );
		bundle.put( SEEN, seen );
		bundle.put( TYPE, type );
		bundle.put( ITEMS, items );
		bundle.put( HAUNTED, haunted );
		bundle.put( AUTO_EXPLORED, autoExplored );
	}
	
}