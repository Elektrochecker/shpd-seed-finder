package com.shatteredpixel.shatteredpixeldungeon;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GoldenMimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CorpseDust;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.items.journal.Guidebook;
import com.watabou.utils.Random;
import com.watabou.noosa.Game;

public class SeedFinder {
	enum Condition {ANY, ALL};

	public static class Options {
		public static int floors;
		public static Condition condition;
		public static String itemListFile;
		public static String ouputFile;
		public static long seed;

		public static boolean searchForDaily;
		public static int DailyOffset;
	}

	public class HeapItem {
		public Item item;
		public Heap heap;

		public HeapItem(Item item, Heap heap) {
			this.item = item;
			this.heap = heap;
		}
	}

	List<Class<? extends Item>> blacklist;
	ArrayList<String> itemList;

	private void parseArgs(String[] args) {
		if (args.length == 2) {
			Options.ouputFile = "stdout";
			Options.floors = Integer.parseInt(args[0]);
			Options.seed = DungeonSeed.convertFromText(args[1]);

			if (args[1].contains("daily")) {
				Options.searchForDaily = true;
				String offsetNumber = args[1].replace("daily", "");

				if (offsetNumber != "") {
					Options.DailyOffset = Integer.valueOf(offsetNumber);
				}
				
			}

			return;			
		}

		Options.floors = Integer.parseInt(args[0]);
		Options.condition = args[1].equals("any") ? Condition.ANY : Condition.ALL;
		Options.itemListFile = args[2];

		if (args.length < 4)
			Options.ouputFile = "out.txt";

		else
			Options.ouputFile = args[3];
	}

	private ArrayList<String> getItemList() {
		ArrayList<String> itemList = new ArrayList<>();

		try {
			Scanner scanner = new Scanner(new File(Options.itemListFile));

			while (scanner.hasNextLine()) {
				itemList.add(scanner.nextLine());
			}

			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return itemList;
	}

	private void addTextItems(String caption, ArrayList<HeapItem> items, StringBuilder builder) {
		if (!items.isEmpty()) {
			builder.append(caption + ":\n");

			for (HeapItem item : items) {
				Item i = item.item;
				Heap h = item.heap;

				String cursed = "";

				if  (((i instanceof Armor && ((Armor) i).hasGoodGlyph()) || (i instanceof Weapon && ((Weapon) i).hasGoodEnchant()) || (i instanceof Wand) || (i instanceof Artifact)) && i.cursed) {

					cursed = "cursed ";
				}

				if (i instanceof Scroll || i instanceof Potion || i instanceof Ring) {
					int txtLength = i.title().length();

					if(i.cursed) {
						builder.append("- cursed ");
						txtLength += 7;
					} else {
						builder.append("- ");
					}

					//make anonymous names show in the same column to look nice
					String tabstring = "";
					for (int j = 0; j < Math.max(1, 33 - txtLength); j++) {
						tabstring += " ";
					}

					builder.append(i.title().toLowerCase() + tabstring); //item
					builder.append(i.anonymousName().toLowerCase().replace(" potion", "").replace("scroll of ", "").replace(" ring", "")); //color, rune or gem

					//if both location and type are logged only space to the right once
					if (h.type != Type.HEAP) {
						builder.append(" (" + h.title().toLowerCase() + ")");
					}
				} else {
					builder.append("- "+ cursed + i.title().toLowerCase());

					//also make item location log in the same column
					if (h.type != Type.HEAP) {
						for (int j = 0; j < 33 - i.title().length() - cursed.length(); j++) {
							builder.append(" ");
						}

					builder.append("(" + h.title().toLowerCase() + ")");
					}
				}
				builder.append("\n");
			}

			builder.append("\n");
		}
	}

	private void addTextQuest(String caption, ArrayList<Item> items, StringBuilder builder) {
		if (!items.isEmpty()) {
			builder.append(caption + ":\n");

			for (Item i : items) {
				if (i.cursed)
					builder.append("- cursed " + i.title().toLowerCase() + "\n");

				else
					builder.append("- " + i.title().toLowerCase() + "\n");
			}

			builder.append("\n");
		}
	}

    public SeedFinder(String[] args) {
		System.out.print("Elektrocheckers seed finder for SHPD v" + Game.version + "\n");

		parseArgs(args);

		if (args.length == 2) {
			logSeedItems(Long.toString(Options.seed), Options.floors);

			return;
		}

		itemList = getItemList();

		try {
			Writer outputFile = new FileWriter(Options.ouputFile);
			outputFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		String seedDigits = Integer.toString(Random.Int(542900));
		for (int i = Random.Int(9999999); i < DungeonSeed.TOTAL_SEEDS; i++) {
			if (testSeed(seedDigits + Integer.toString(i), Options.floors)) {
				System.out.printf("Found valid seed %s (%d)\n", DungeonSeed.convertToCode(Dungeon.seed), Dungeon.seed);
				logSeedItems(seedDigits + Integer.toString(i), Options.floors);
			}
		}
	}

	private ArrayList<Heap> getMobDrops(Level l) {
		ArrayList<Heap> heaps = new ArrayList<>();

		for (Mob m : l.mobs) {
			if (m instanceof Statue) {
				Heap h = new Heap();
				h.items = new LinkedList<>();
				h.items.add(((Statue) m).weapon.identify());
				h.type = Type.STATUE;
				heaps.add(h);
			}

			else if (m instanceof ArmoredStatue) {
				Heap h = new Heap();
				h.items = new LinkedList<>();
				h.items.add(((ArmoredStatue) m).armor.identify());
				h.items.add(((ArmoredStatue) m).weapon.identify());
				h.type = Type.STATUE;
				heaps.add(h);
			}

			else if (m instanceof Mimic) {
				Heap h = new Heap();
				h.items = new LinkedList<>();

				for (Item item : ((Mimic) m).items)
					h.items.add(item.identify());

				if (m instanceof GoldenMimic) h.type = Type.GOLDEN_MIMIC;
				else if (m instanceof CrystalMimic) h.type = Type.CRYSTAL_MIMIC;
				else h.type = Type.MIMIC;
				heaps.add(h);
			}
		}

		return heaps;
	}

	private boolean testSeed(String seed, int floors) {
		SPDSettings.customSeed(seed);
		GamesInProgress.selectedClass = HeroClass.WARRIOR;
		Dungeon.init();

		boolean[] itemsFound = new boolean[itemList.size()];

		for (int i = 0; i < floors; i++) {

			Level l = Dungeon.newLevel();

			//skip boss floors
			if (Dungeon.depth % 5 != 0) continue;

				ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
				heaps.addAll(getMobDrops(l));

				//check heap items
				for (Heap h : heaps) {
					for (Item item : h.items) {
						item.identify();

						for (int j = 0; j < itemList.size(); j++) {
							if (item.title().toLowerCase().contains(itemList.get(j))
								|| item.anonymousName().toLowerCase().contains(itemList.get(j))) {
								if (itemsFound[j] == false) {
									itemsFound[j] = true;
									break;
								}
							}
						}
					}
				}

				//check sacrificial fire
				if (l.sacrificialFireItem != null) {
					for (int j = 0; j < itemList.size(); j++) {
						if (l.sacrificialFireItem.title().toLowerCase().contains(itemList.get(j))) {
							if (!itemsFound[j]) {
								itemsFound[j] = true;
								break;
							}
					}
				}

				//check quests
				Item[] questitems = {
					Ghost.Quest.armor,
					Ghost.Quest.weapon,
					Wandmaker.Quest.wand1,
					Wandmaker.Quest.wand2,
					Imp.Quest.reward
				};

				if (Ghost.Quest.armor != null) {
					questitems[0] = Ghost.Quest.armor.inscribe(Ghost.Quest.glyph);
					questitems[1] = Ghost.Quest.weapon.enchant(Ghost.Quest.enchant);
				}

				for (int j = 0; j < itemList.size(); j++) {
					for (int k = 0; k < 5; k++) {
						if (questitems[k] != null) {
							if (questitems[k].identify().title().toLowerCase().contains(itemList.get(j))) {
								if (!itemsFound[j]) {
									itemsFound[j] = true;
									break;
								}
							}
						}
					}
				}
			}

			Dungeon.depth++;
		}

		if (Options.condition == Condition.ANY) {
			for (int i = 0; i < itemList.size(); i++) {
				if (itemsFound[i] == true)
					return true;
			}

			return false;
		}

		else {
			for (int i = 0; i < itemList.size(); i++) {
				if (itemsFound[i] == false)
					return false;
			}

			return true;
		}
	}

	private void logSeedItems(String seed, int floors) {
		PrintWriter out = null;
		OutputStream out_fd = System.out;

		try {
			if (Options.ouputFile != "stdout")
				out_fd = new FileOutputStream(Options.ouputFile, true);

			out = new PrintWriter(out_fd);
		} catch (FileNotFoundException e) { // gotta love Java mandatory exceptions
			e.printStackTrace();
		}
		
		String seedinfotext = "";

		if (Options.searchForDaily) {
			Dungeon.daily = true;
			long DAY = 1000 * 60 * 60 * 24;
			long currentDay = (long) Math.floor(Game.realTime/DAY) + Options.DailyOffset;
			SPDSettings.lastDaily(DAY * currentDay);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.init();
			seedinfotext += format.format(new Date(SPDSettings.lastDaily()));

			out.printf("Items for daily run %s (%d):\n\n", seedinfotext, Dungeon.seed);
		} else {
			Dungeon.daily = false;
			SPDSettings.customSeed(seed);

			GamesInProgress.selectedClass = HeroClass.WARRIOR;
			Dungeon.init();
			seedinfotext += DungeonSeed.convertToCode(Dungeon.seed);

			out.printf("Items for seed %s (%d):\n\n", seedinfotext, Dungeon.seed);
		}

		
		

		blacklist = Arrays.asList(Gold.class, Dewdrop.class, IronKey.class, GoldenKey.class, CrystalKey.class, EnergyCrystal.class,
								  CorpseDust.class, Embers.class, CeremonialCandle.class, Pickaxe.class, Guidebook.class);
		

		for (int i = 0; i < floors; i++) {

			Level l = Dungeon.newLevel();
			ArrayList<Heap> heaps = new ArrayList<>(l.heaps.valueList());
			StringBuilder builder = new StringBuilder();
			ArrayList<HeapItem> scrolls = new ArrayList<>();
			ArrayList<HeapItem> potions = new ArrayList<>();
			ArrayList<HeapItem> equipment = new ArrayList<>();
			ArrayList<HeapItem> rings = new ArrayList<>();
			ArrayList<HeapItem> artifacts = new ArrayList<>();
			ArrayList<HeapItem> wands = new ArrayList<>();
			ArrayList<HeapItem> others = new ArrayList<>();

			out.printf("--- floor %d: ", Dungeon.depth);

			String feeling = l.feeling.toString();

			switch (feeling) {
				case "NONE":
					feeling = "no feeling";
					break;
				case "CHASM":
					feeling = "chasms";
					break;
				case "WATER":
					feeling = "water";
					break;
				case "GRASS":
					feeling = "vegetation";
					break;
				case "DARK":
					feeling = "enemies moving in the darkness";
					break;
				case "LARGE":
					feeling = "unusually large";
					break;
				case "TRAPS":
					feeling = "traps";
					break;
				case "SECRETS":
					feeling = "secrets";
					break;
			}

			switch (Dungeon.depth) {
				case 5:
					feeling = "goo";
					break;
				
				case 10:
					feeling = "tengu";
					break;
				
				case 15:
					feeling = "DM-300";
					break;
				
				case 20:
					feeling = "dwarven king";
					break;
				
				case 25:
					feeling = "yog dzewa";
					break;
			}

			out.printf(feeling + "\n\n");

			// list quest rewards
			if (Ghost.Quest.armor != null) {
				ArrayList<Item> rewards = new ArrayList<>();
				rewards.add(Ghost.Quest.armor.inscribe(Ghost.Quest.glyph).identify());
				rewards.add(Ghost.Quest.weapon.enchant(Ghost.Quest.enchant).identify());
				Ghost.Quest.complete();

				addTextQuest("Ghost quest rewards", rewards, builder);
			}

			if (Wandmaker.Quest.wand1 != null) {
				ArrayList<Item> rewards = new ArrayList<>();
				rewards.add(Wandmaker.Quest.wand1.identify());
				rewards.add(Wandmaker.Quest.wand2.identify());
				Wandmaker.Quest.complete();

				builder.append("Wandmaker quest item: ");

				switch (Wandmaker.Quest.type) {
					case 1: default:
						builder.append("corpse dust\n\n");
						break;
					case 2:
						builder.append("fresh embers\n\n");
						break;
					case 3:
						builder.append("rotberry seed\n\n");
				}

				addTextQuest("Wandmaker quest rewards", rewards, builder);
			}

			if (Imp.Quest.reward != null) {
				ArrayList<Item> rewards = new ArrayList<>();
				rewards.add(Imp.Quest.reward.identify());
				Imp.Quest.complete();

				addTextQuest("Imp quest reward", rewards, builder);
			}

			//sacrificial fire
			if (l.sacrificialFireItem != null) {
				Item fireItem = l.sacrificialFireItem.identify();

				builder.append("- " + fireItem.title().toLowerCase() + " (sacrificial fire)");

				builder.append("\n\n");
			}

			heaps.addAll(getMobDrops(l));

			// list items
			for (Heap h : heaps) {
				for (Item item : h.items) {
					item.identify();

					if (h.type == Type.FOR_SALE) continue;
					else if (blacklist.contains(item.getClass())) continue;
					else if (item instanceof Scroll) scrolls.add(new HeapItem(item, h));
					else if (item instanceof Potion) potions.add(new HeapItem(item, h));
					else if (item instanceof MeleeWeapon || item instanceof Armor) equipment.add(new HeapItem(item, h));
					else if (item instanceof Ring) rings.add(new HeapItem(item, h));
					else if (item instanceof Wand) wands.add(new HeapItem(item, h));
					else if (item instanceof Artifact) {
						 artifacts.add(new HeapItem(item, h));
					} else others.add(new HeapItem(item, h));
				}
			}

			addTextItems("Equipment", equipment, builder);
			addTextItems("Scrolls", scrolls, builder);
			addTextItems("Potions", potions, builder);
			addTextItems("Rings", rings, builder);
			addTextItems("Artifacts", artifacts, builder);
			addTextItems("Wands", wands, builder);
			addTextItems("Other", others, builder);

			out.print(builder.toString());

			Dungeon.depth++;
		}

		out.close();
    }
}
