import java.util.Random;
import tester.*; // The tester library
import javalib.worldimages.*; // images, like RectangleImage or OverlayImages
import javalib.funworld.*; // the abstract World class and the big-bang library
import java.awt.Color;

//abstract class to represent a fish
abstract class AFish {
  Posn position;
  int size;
  Color color;

  //constructor for AFish class
  AFish(Posn position, int size, Color color) {
    this.position = position;
    this.size = size;
    this.color = color;
  }

  // checking if this fish can eat the other
  public boolean canEat(AFish other) {
    return this.collidesWith(other)
      && this.size > other.size;
  }

  // Method to check if two fish collide
  public boolean collidesWith(AFish other) {
    double distance = Math.sqrt(
      Math.pow(this.position.x - other.position.x, 2)
        + Math.pow(this.position.y - other.position.y, 2));
    return distance < (this.size / 2 + other.size / 2);
  }

  // Method to keep the AFish in bounds
  public AFish keepInBounds() {
    if (this.position.x - this.size > 1000) {
      this.position.x = 0 - this.size;
    }
    if (this.position.y - this.size > 800) {
      this.position.y = 0 - this.size;
    }
    if (this.position.x + this.size < 0) {
      this.position.x = 1000 + this.size;
    }
    if (this.position.y + this.size < 0) {
      this.position.y = 800 + this.size;
    }
    return this;
  }

  //declaration of a function to move the fish
  abstract AFish move();
  // drawing the world
  abstract WorldImage draw();
}
// represent a list of fish in the background
interface ILoBGFish {
  //to draw the list of fish
  WorldScene draw(WorldScene acc);

  //to move the list of fish
  ILoBGFish moveFish();

  //to check overlap between the player and list of fish
  ILoBGFish checkOverlap(PlayerFish pf);

  //to check if the player is eaten by a fish in the list
  boolean playerIsEaten(PlayerFish pf);

  //to check if the player is bigger than the rest of the fish in the list
  boolean playerIsLargest(PlayerFish pf);
}
//to represent an empty list of fish
class MtLoBGFish implements ILoBGFish {
  //to draw an empty list of fish
  public WorldScene draw(WorldScene acc) {
    return acc;
  }

  //to move an empty list of fish
  public ILoBGFish moveFish() {
    return this;
  }

  //to check the overlap between the player and list of fish
  public ILoBGFish checkOverlap(PlayerFish pf) {
    return this;
  }

  //to check if the player is eaten by the list of fish
  public boolean playerIsEaten(PlayerFish pf) {
    return false;
  }

  //to check if the player is larger than the other fish in the list
  public boolean playerIsLargest(PlayerFish pf) {
    return true;
  }
}
//to represent a non-empty list of fish
class ConsLoBGFish implements ILoBGFish {
  AFish first;
  ILoBGFish rest;

  //constructor for a non-empty list of fish
  ConsLoBGFish(AFish first, ILoBGFish rest) {
    this.first = first;
    this.rest = rest;
  }

  //to draw a list of fish
  public WorldScene draw(WorldScene acc) {
    return this.rest.draw(acc.placeImageXY(this.first.draw(), this.first.position.x,
      this.first.position.y));
  }

  //to move the list of fish
  public ILoBGFish moveFish() {
    return new ConsLoBGFish(this.first.move(), this.rest.moveFish());
  }

  //to check overlap between the player and the fish in the list
  public ILoBGFish checkOverlap(PlayerFish pf) {
    if (pf.canEat(this.first)) {
      return this.rest.checkOverlap(pf.eat(this.first));
    }
    else {
      return new ConsLoBGFish(this.first, this.rest.checkOverlap(pf));
    }
  }

  //to check if the player has been eaten by any fish in the list
  public boolean playerIsEaten(PlayerFish pf) {
    return this.first.canEat(pf)
      || this.rest.playerIsEaten(pf);
  }

  //to check if the player is larger than all the fish in the list
  public boolean playerIsLargest(PlayerFish pf) {
    return pf.size > 120;
  }
}

//class to represent the player
class PlayerFish extends AFish {
  int xVel;
  int yVel;
  int score;

  //constructor for a playerFish
  PlayerFish(Posn position, int size, Color color, int xVel, int yVel, int score) {
    super(position, size, color);
    this.xVel = xVel;
    this.yVel = yVel;
    this.score = score;
  }

  //checks if the fish has moved out of bounds, and if it has
  //the fish loops around the other side of the screen
  public AFish move() {
    this.position.x += this.xVel;
    this.position.y += this.yVel;
    this.keepInBounds();
    return new PlayerFish(new Posn(this.position.x, this.position.y), size, color, xVel, yVel, score);
  }

  //to draw a single fish
  public WorldImage draw() {
    return new BesideAlignImage(AlignModeY.PINHOLE, new CircleImage(size, OutlineMode.SOLID, this.color),
      new RotateImage(new EquilateralTriangleImage(size, OutlineMode.SOLID, this.color), 270));
  }

  //for when a player eats another fish and grows in size
  public PlayerFish eat(AFish other) {
    this.size += other.size;
    return new PlayerFish(new Posn(this.position.x, this.position.y), size, color, xVel, yVel, score += 1);
  }
}

//class to represent a background fish
class BackgroundFish extends AFish {
  Random rand;

  //constructor for a background fish
  BackgroundFish(Posn position, int size, Color color, Random rand) {
    super(position, size, color);
    this.rand = rand;
  }

  //second constructor for a random background fish
  BackgroundFish(Random rand) {
    super(new Posn(rand.nextInt(800), rand.nextInt(600)),
      rand.nextInt(120), Color.RED);
    this.rand = rand;
  }

  //to draw a background fish
  public WorldImage draw() {
    return new BesideAlignImage(AlignModeY.PINHOLE, new CircleImage(size, OutlineMode.SOLID, this.color),
      new RotateImage(new EquilateralTriangleImage(size, OutlineMode.SOLID, this.color), 270));
  }

  //checks if the fish has been moved out of bounds, and if it has
  //the fish loops around the other end of the screen
  public AFish move() {
    this.keepInBounds();
    return new BackgroundFish(new Posn(this.position.x + randomSpeed(rand.nextInt(100)),
      this.position.y + randomSpeed(rand.nextInt(100))
        * randomDirection(rand.nextInt(100))), size, color, rand);
  }

  // to set a random percentage between two set speeds
  public int randomSpeed(int rand) {
    if (rand < 50) {
      return 25;
    }
    else {
      return 10;
    }
  }

  // to set a random positive or negative direction
  public int randomDirection(int rand) {
    if (rand < 50) {
      return 1;
    }
    else {
      return -1;
    }
  }
}

//to represent the World
class FishWorld extends World {
  PlayerFish player;
  ILoBGFish bgfish;

  //constructor for the World
  FishWorld(PlayerFish player, ILoBGFish bgfish) {
    this.player = player;
    this.bgfish = bgfish;
  }

  //checks if the player and any of the fish are overlapping
  public ILoBGFish fishCheckerHelp(ILoBGFish fish) {
    return fish.checkOverlap(this.player);
  }

  //make the world scene, including the ending screens
  public WorldScene makeScene() {
    if (bgfish.playerIsEaten(player)) {
      return new WorldScene(1000, 800).placeImageXY(
        new AboveImage(new TextImage("Game Over.", 100, Color.red),
          new TextImage("Fish Eaten: " + Integer.toString(player.score), 40, Color.black)),  500, 350);
    }
    else if (bgfish.playerIsLargest(player)) {
      return new WorldScene(1000, 800).placeImageXY(
        new AboveImage(new TextImage("You Won!", 100, Color.green),
          new TextImage("Fish Eaten: " + Integer.toString(player.score), 40, Color.black)),  500, 350);
    }
    else {
      WorldScene world = new WorldScene(1000, 800);
      world = this.bgfish.draw(world);
      return world.placeImageXY(player.draw(), player.position.x, player.position.y);
    }
  }

  // Check which key was pressed and update the player fish's position accordingly
  public World onKeyEvent(String key) {
    int stepSize = 3;
    if (key.equals("up")) {
      player.yVel -= stepSize;
    } else if (key.equals("down")) {
      player.yVel += stepSize;
    } else if (key.equals("left")) {
      player.xVel -= stepSize;
    } else if (key.equals("right")) {
      player.xVel += stepSize;
    }
    return new FishWorld(player, bgfish);
  }

  //every tick rate, moves the fish and checks overlap
  public World onTick() {
    int stepSize = 3;
    if (player.yVel > 0) {
      player.yVel -= stepSize;
    }
    if (player.yVel < 0) {
      player.yVel += stepSize;
    }
    if (player.xVel < 12) {
      player.xVel += stepSize;
    }
    if (bgfish.playerIsEaten(player)) {
      return this.endOfWorld("game Over");
    }
    else {
      this.player.move();
      ILoBGFish fishes = this.bgfish.moveFish();
      this.bgfish = this.fishCheckerHelp(fishes);
      return this;
    }
  }
}

// examples of Fish
class ExamplesFish {
  // examples for fishPlayer
  PlayerFish fishplayer = new PlayerFish(new Posn(950, 750), 30, Color.blue, 0, 0, -1);
  PlayerFish secondPlayer = new PlayerFish(new Posn(500, 500), 250, Color.blue, 0, 0, 0);
  PlayerFish thirdPlayer = new PlayerFish(new Posn(400, 200), 10, Color.blue, 0, 0, 0);

  // examples of BackgroundFish
  AFish fish5 = new BackgroundFish(new Posn(400, 200), 40, Color.red, new Random());
  AFish fish6 = new BackgroundFish(new Posn(600, 600), 150, Color.red, new Random());
  AFish fish7 = new BackgroundFish(new Posn(400, 200), 50, Color.red, new Random());
  AFish fish4 = new BackgroundFish(new Random());
  AFish fish3 = new BackgroundFish(new Random());
  AFish fish2 = new BackgroundFish(new Random());
  AFish fish1 = new BackgroundFish(new Random());
  // examples of ILoBgFish
  ILoBGFish mt = new MtLoBGFish();
  ILoBGFish list0 = new ConsLoBGFish(fish7, mt);
  ILoBGFish list1 = new ConsLoBGFish(fish1, (new ConsLoBGFish(fish2, mt)));
  ILoBGFish list2 = new ConsLoBGFish(fish1, (new ConsLoBGFish(fish2,
    new ConsLoBGFish(fish3, mt))));
  ILoBGFish list3 = new ConsLoBGFish(fish1, (new ConsLoBGFish(fish2,
    new ConsLoBGFish(fish3, new ConsLoBGFish(fish4, mt)))));
  ILoBGFish list4 = new ConsLoBGFish(fish5, (new ConsLoBGFish(fish6, mt)));
  ILoBGFish list5 = new ConsLoBGFish(fish5, (new ConsLoBGFish(fish7, mt)));

  // to test the BigBang method
  boolean testBigBang(Tester t) {
    FishWorld world = new FishWorld(this.fishplayer, this.list3);
    int worldWidth = 1000;
    int worldHeight = 800;
    double tickRate = 0.1;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }

  // to test the canEat method
  boolean testCanEat(Tester t) {
    return t.checkExpect(fishplayer.canEat(fish5), false)
      && t.checkExpect(fishplayer.canEat(fish6), false);
  }

  // to test the collidesWith method
  boolean testCollidesWith(Tester t) {
    return t.checkExpect(fishplayer.collidesWith(fish5), false)
      && t.checkExpect(fishplayer.collidesWith(fish6), false);
  }

  // to test the eat method
  boolean testEat(Tester t) {
    return t.checkExpect(fishplayer.eat(fish5),
      new PlayerFish(new Posn(950, 750), 70, Color.blue, 0, 0, 0));
  }

  boolean testCheckOverlap(Tester t) {
    return t.checkExpect(list5.checkOverlap(fishplayer), list5)
      && t.checkExpect(list4.checkOverlap(fishplayer), list4);
  }

  // to test the playerIsEaten method
  boolean testPlayerIsEaten(Tester t) {
    return t.checkExpect(list3.playerIsEaten(secondPlayer), false)
      && t.checkExpect(list5.playerIsEaten(fishplayer), false)
      && t.checkExpect(list0.playerIsEaten(thirdPlayer), true);
  }

  // to test the playerIsLargest method
  boolean testPlayerIsLargest(Tester t) {
    return t.checkExpect(list3.playerIsLargest(secondPlayer), true)
      && t.checkExpect(list4.playerIsLargest(fishplayer), false);
  }

  // method defined in the Examples class for testing
  public int randomSpeed(int rand) {
    if (rand < 50) {
      return 25;
    }
    else {
      return 10;
    }
  }

  // method defined in the Examples class for testing
  public int randomDirection(int rand) {
    if (rand < 50) {
      return 1;
    }
    else {
      return -1;
    }
  }

  // to test the randomSpeed method
  boolean testRandomSpeed(Tester t) {
    return t.checkExpect(randomSpeed(10), 25)
      && t.checkExpect(randomSpeed(60), 10);
  }

  // to test the randomDirection method
  boolean testRandomDirection(Tester t) {
    return t.checkExpect(randomDirection(10), 1)
      && t.checkExpect(randomDirection(60), -1);
  }
}

