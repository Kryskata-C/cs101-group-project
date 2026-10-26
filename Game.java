import java.io.*;
import java.util.*;

// ===================== 1. CHARACTERS =====================

abstract class Character {
    String name;
    int hp, maxHp, attack, defense;
    int poisonTurns = 0;

    Character(String name, int hp, int attack, int defense) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
    }

    boolean isAlive() {
        return hp > 0;
    }

    void takeDamage(int amount) {
        int damage = Math.max(1, amount - defense);
        hp = Math.max(0, hp - damage);
        System.out.println(name + " takes " + damage + " damage (HP " + hp + ")");
    }

    void attack(Character target) {
        System.out.println(name + " attacks " + target.name + "!");
        target.takeDamage(attack);
    }

    // Status effect: poison hurts a little every turn
    void tickStatus() {
        if (poisonTurns > 0 && isAlive()) {
            hp = Math.max(0, hp - 3);
            poisonTurns--;
            System.out.println(name + " is hurt by poison (HP " + hp + ")");
        }
    }
}

class Player extends Character {
    int level = 1, xp = 0, gold = 20;
    int mana = 20, maxMana = 20;
    Weapon weapon;
    Armor armor;
    ArrayList<Item> inventory = new ArrayList<>();
    ArrayList<Skill> skills = new ArrayList<>();

    Player(String name) {
        super(name, 100, 10, 3);
        skills.add(new Skill("Fireball", 6, 18, 0));
        skills.add(new Skill("Poison Stab", 4, 6, 3));
        inventory.add(new Potion("Small Potion", 10, 25));
    }

    void useSkill(Character target) {
        for (int i = 0; i < skills.size(); i++) {
            System.out.println((i + 1) + ") " + skills.get(i));
        }
        int choice = Game.readInt("Skill: ");
        if (choice < 1 || choice > skills.size()) return;
        skills.get(choice - 1).use(this, target);
    }

    // ===================== 6. LEVELLING =====================
    void gainXp(int amount) {
        xp += amount;
        System.out.println("You gain " + amount + " XP.");
        while (xp >= level * 30) {
            xp -= level * 30;
            level++;
            maxHp += 10;
            hp = maxHp;
            attack += 2;
            defense += 1;
            maxMana += 5;
            mana = maxMana;
            System.out.println("*** LEVEL UP! You are now level " + level + " ***");
            if (level == 3) {
                skills.add(new Skill("Lightning", 10, 30, 0));
                System.out.println("You learned Lightning!");
            }
        }
    }
}

// ===================== 2. ENEMIES AND BOSSES =====================

class Enemy extends Character {
    int xpReward, goldReward;

    Enemy(String name, int hp, int attack, int defense, int xpReward, int goldReward) {
        super(name, hp, attack, defense);
        this.xpReward = xpReward;
        this.goldReward = goldReward;
    }
}

class Spider extends Enemy {
    Spider() {
        super("Spider", 25, 7, 1, 25, 10);
    }

    @Override
    void attack(Character target) {
        super.attack(target);
        target.poisonTurns = 3;
        System.out.println(target.name + " is poisoned!");
    }
}

class Boss extends Enemy {
    int turn = 0;

    Boss() {
        super("Dragon", 100, 14, 5, 100, 100);
    }

    @Override
    void attack(Character target) {
        turn++;
        if (turn % 3 == 0) {
            System.out.println("The Dragon breathes FIRE!");
            target.takeDamage(attack * 2);
        } else {
            super.attack(target);
        }
    }
}

// ===================== 3. ITEMS =====================

abstract class Item {
    String name;
    int price;

    Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    abstract void use(Player player);
}

class Weapon extends Item {
    int damage;

    Weapon(String name, int price, int damage) {
        super(name, price);
        this.damage = damage;
    }

    void use(Player player) {
        if (player.weapon != null) player.attack -= player.weapon.damage;
        player.weapon = this;
        player.attack += damage;
        System.out.println("You equip the " + name + " (+" + damage + " attack)");
    }
}

class Armor extends Item {
    int protection;

    Armor(String name, int price, int protection) {
        super(name, price);
        this.protection = protection;
    }

    void use(Player player) {
        if (player.armor != null) player.defense -= player.armor.protection;
        player.armor = this;
        player.defense += protection;
        System.out.println("You wear the " + name + " (+" + protection + " defense)");
    }
}

class Potion extends Item {
    int heal;

    Potion(String name, int price, int heal) {
        super(name, price);
        this.heal = heal;
    }

    void use(Player player) {
        player.hp = Math.min(player.maxHp, player.hp + heal);
        player.poisonTurns = 0;
        System.out.println("You drink the " + name + " (HP " + player.hp + ")");
    }
}

// ===================== 4. SKILLS =====================

class Skill {
    String name;
    int manaCost, damage, poisonTurns;

    Skill(String name, int manaCost, int damage, int poisonTurns) {
        this.name = name;
        this.manaCost = manaCost;
        this.damage = damage;
        this.poisonTurns = poisonTurns;
    }

    void use(Player player, Character target) {
        if (player.mana < manaCost) {
            System.out.println("Not enough mana!");
            return;
        }
        player.mana -= manaCost;
        System.out.println(player.name + " casts " + name + "!");
        target.takeDamage(damage);
        if (poisonTurns > 0) {
            target.poisonTurns = poisonTurns;
            System.out.println(target.name + " is poisoned!");
        }
    }

    public String toString() {
        return name + " (" + manaCost + " mana, " + damage + " damage)";
    }
}

// ===================== 5. DUNGEON (the game itself) =====================

public class Game {
    static Random random = new Random();
    static Player player;
    static char[][] map;
    static int row, col;
    static boolean bossDefeated = false;
    static String message = "";

    public static void main(String[] args) throws Exception {
        System.out.println("===== DUNGEON CRAWLER =====");
        System.out.print("1) New game  2) Load game: ");
        if (readLine().equals("2") && load()) {
            message = "Game loaded!";
        } else {
            newGame();
        }

        // Read keys instantly (no Enter needed) and put the terminal back when the game closes
        setRawMode(true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> setRawMode(false)));

        // Game loop: react to keys straight away, move enemies and redraw every second
        long lastTick = System.currentTimeMillis();
        draw();
        while (player.isAlive() && !bossDefeated) {
            if (System.in.available() > 0) {
                char key = readKey();
                if (key == 'w') move(-1, 0);
                else if (key == 's') move(1, 0);
                else if (key == 'a') move(0, -1);
                else if (key == 'd') move(0, 1);
                else if (key == 'i') { useItem(); pause(); }
                else if (key == 'k') save();
                else if (key == 'q') return;
                draw();
            }
            if (System.currentTimeMillis() - lastTick >= 1000) {
                moveEnemies();
                draw();
                lastTick = System.currentTimeMillis();
            }
            Thread.sleep(50);
        }

        if (bossDefeated) System.out.println("\nYou slayed the Dragon. YOU WIN!");
        else System.out.println("\nGAME OVER");
    }

    static void newGame() {
        System.out.print("Enter your name: ");
        player = new Player(readLine());

        // # wall  . floor  P player  E enemy  B boss  ? item  $ shop
        String[] layout = {
            "#########################################",
            "#P.......#...........#.........?........#",
            "#........#...E.......#..................#",
            "#...?....#...........#......E...........#",
            "#........#####.#######..................#",
            "#..E.....#...........#######.############",
            "#..........E.........#..........#.......#",
            "#####.####...........#....E.....#...$...#",
            "#........#...........#..........#.......#",
            "#........#######.#####..........#.......#",
            "#...E........#.........?........###.#####",
            "#............#..........................#",
            "#.....?......#....E.........E.....##.####",
            "#............#....................#....B#",
            "#########################################"
        };
        map = new char[layout.length][];
        for (int i = 0; i < layout.length; i++) {
            map[i] = layout[i].toCharArray();
        }
        findPlayer();
    }

    static void findPlayer() {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                if (map[r][c] == 'P') {
                    row = r;
                    col = c;
                }
            }
        }
    }

    // Clears the screen and prints the whole 2D array again
    static void draw() {
        System.out.print("\033[H\033[2J");
        for (char[] line : map) {
            System.out.println(new String(line));
        }
        System.out.println(player.name + "  Lv " + player.level + "  HP " + player.hp + "/" + player.maxHp
                + "  MP " + player.mana + "  XP " + player.xp + "/" + (player.level * 30) + "  Gold " + player.gold);
        System.out.println("w/a/s/d move   i items   k save   q quit");
        System.out.println(message);
        System.out.flush();
    }

    // Moving updates the 2D array: old square becomes '.', new square becomes 'P'
    static void move(int dRow, int dCol) {
        int newRow = row + dRow;
        int newCol = col + dCol;
        char tile = map[newRow][newCol];
        message = "";

        if (tile == '#') {
            message = "A wall blocks the way.";
            return;
        }
        if (tile == '$') {
            shop();
            pause();
            return;
        }
        if (tile == 'E' && !fight(randomEnemy())) return;
        if (tile == 'B') {
            if (!fight(new Boss())) return;
            bossDefeated = true;
        }
        if (tile == '?') {
            Item item = randomItem();
            player.inventory.add(item);
            message = "You found a " + item.name + "!";
        }

        map[row][col] = '.';
        row = newRow;
        col = newCol;
        map[row][col] = 'P';
    }

    // Every second each enemy takes one random step; walking into the player starts a fight
    static void moveEnemies() {
        ArrayList<int[]> enemies = new ArrayList<>();
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                if (map[r][c] == 'E') enemies.add(new int[] {r, c});
            }
        }

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] enemy : enemies) {
            if (!player.isAlive()) return;
            int[] dir = directions[random.nextInt(4)];
            int r = enemy[0] + dir[0];
            int c = enemy[1] + dir[1];

            if (map[r][c] == '.') {
                map[enemy[0]][enemy[1]] = '.';
                map[r][c] = 'E';
            } else if (map[r][c] == 'P') {
                draw();
                if (fight(randomEnemy())) map[enemy[0]][enemy[1]] = '.';
            }
        }
    }

    // Returns true if the player won the fight
    static boolean fight(Enemy enemy) {
        System.out.println("\nA " + enemy.name + " appears!");
        while (player.isAlive() && enemy.isAlive()) {
            System.out.println("\n" + player.name + " HP " + player.hp + "  MP " + player.mana
                    + "   vs   " + enemy.name + " HP " + enemy.hp);
            int choice = readInt("1) Attack  2) Skill  3) Item  4) Run: ");

            if (choice == 1) player.attack(enemy);
            else if (choice == 2) player.useSkill(enemy);
            else if (choice == 3) useItem();
            else if (choice == 4) {
                if (random.nextBoolean()) {
                    System.out.println("You ran away!");
                    pause();
                    return false;
                }
                System.out.println("You couldn't escape!");
            } else continue;

            if (enemy.isAlive()) enemy.attack(player);
            player.tickStatus();
            enemy.tickStatus();
        }

        if (!player.isAlive()) return false;
        System.out.println("You defeated the " + enemy.name + "! +" + enemy.goldReward + " gold");
        player.gold += enemy.goldReward;
        player.mana = player.maxMana;
        player.gainXp(enemy.xpReward);
        pause();
        return true;
    }

    static Enemy randomEnemy() {
        if (random.nextBoolean()) return new Spider();
        return new Enemy("Goblin", 30, 8, 1, 20, 15);
    }

    static Item randomItem() {
        int n = random.nextInt(3);
        if (n == 0) return new Weapon("Iron Sword", 25, 4);
        if (n == 1) return new Armor("Leather Armor", 25, 2);
        return new Potion("Small Potion", 10, 25);
    }

    static void useItem() {
        if (player.inventory.isEmpty()) {
            System.out.println("Your inventory is empty.");
            return;
        }
        for (int i = 0; i < player.inventory.size(); i++) {
            System.out.println((i + 1) + ") " + player.inventory.get(i).name);
        }
        int choice = readInt("Use item (0 = cancel): ");
        if (choice < 1 || choice > player.inventory.size()) return;
        player.inventory.remove(choice - 1).use(player);
    }

    // ===================== 6. SHOP =====================
    static void shop() {
        Item[] stock = {
            new Potion("Big Potion", 15, 50),
            new Weapon("Steel Sword", 40, 8),
            new Armor("Iron Armor", 40, 4)
        };
        System.out.println("\n===== SHOP =====  Gold: " + player.gold);
        for (int i = 0; i < stock.length; i++) {
            System.out.println((i + 1) + ") " + stock[i].name + " - " + stock[i].price + " gold");
        }
        int choice = readInt("Buy (0 = leave): ");
        if (choice < 1 || choice > stock.length) return;

        Item item = stock[choice - 1];
        if (player.gold < item.price) {
            System.out.println("Not enough gold!");
        } else {
            player.gold -= item.price;
            player.inventory.add(item);
            System.out.println("You bought a " + item.name + ".");
        }
    }

    // ===================== 6. SAVE / LOAD =====================
    static void save() {
        try (PrintWriter out = new PrintWriter("save.txt")) {
            out.println(player.name);
            out.println(player.hp + " " + player.maxHp + " " + player.attack + " " + player.defense + " "
                    + player.mana + " " + player.maxMana + " " + player.level + " " + player.xp + " " + player.gold);
            for (char[] line : map) {
                out.println(new String(line));
            }
            message = "Game saved.";
        } catch (IOException e) {
            message = "Could not save.";
        }
    }

    static boolean load() {
        try (Scanner file = new Scanner(new File("save.txt"))) {
            player = new Player(file.nextLine());
            player.hp = file.nextInt();
            player.maxHp = file.nextInt();
            player.attack = file.nextInt();
            player.defense = file.nextInt();
            player.mana = file.nextInt();
            player.maxMana = file.nextInt();
            player.level = file.nextInt();
            player.xp = file.nextInt();
            player.gold = file.nextInt();
            file.nextLine();

            ArrayList<char[]> rows = new ArrayList<>();
            while (file.hasNextLine()) {
                rows.add(file.nextLine().toCharArray());
            }
            map = rows.toArray(new char[0][]);
            findPlayer();
            return true;
        } catch (Exception e) {
            System.out.println("No save found, starting a new game.");
            return false;
        }
    }

    // ===================== KEYBOARD =====================

    // Turns "press Enter" mode off so every key arrives straight away
    static void setRawMode(boolean on) {
        String command = on ? "stty -icanon -echo min 1 < /dev/tty" : "stty sane < /dev/tty";
        try {
            new ProcessBuilder("sh", "-c", command).inheritIO().start().waitFor();
        } catch (Exception e) {
            System.out.println("Could not change terminal mode.");
        }
    }

    static char readKey() {
        try {
            return java.lang.Character.toLowerCase((char) System.in.read());
        } catch (IOException e) {
            return 'q';
        }
    }

    static String readLine() {
        try {
            StringBuilder text = new StringBuilder();
            int c = System.in.read();
            while (c != '\n' && c != -1) {
                text.append((char) c);
                c = System.in.read();
            }
            return text.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }

    static int readInt(String prompt) {
        System.out.print(prompt);
        char key = readKey();
        System.out.println(key);
        if (java.lang.Character.isDigit(key)) return key - '0';
        return -1;
    }

    static void pause() {
        System.out.println("(press any key)");
        readKey();
    }
}
