import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.awt.Color;
import javalib.worldimages.*;
import tester.Tester;
import javalib.impworld.*;

//class representing a single cell
class Cell {
  int x;
  int y;
  Color color;
  boolean isFlooded;
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  //constructor for the cell
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.isFlooded = false;
    this.left = null;
    this.right = null;
    this.top = null;
    this.bottom = null;
  }

  //draws an individual cell
  public WorldImage drawCell(Color cellColor) {
    return new RectangleImage(20, 20, "solid", cellColor);
  }
}

//class representing the game
class Flood extends World {

  //initializing variables
  ArrayList<Cell> board;
  int boardSize = 20;
  int grid = boardSize * boardSize;
  int numColors;
  final int cellSize = 20;
  Random rand = new Random();
  WorldImage background = new RectangleImage(this.boardSize * this.boardSize,
      this.boardSize * this.boardSize, OutlineMode.SOLID, Color.WHITE);
  HashMap<Integer, Color> colorList = new HashMap<Integer, Color>();
  int numClicks = 25;

  //inserts 7 colors into the hashmap for colors
  HashMap<Integer, Color> createColors() {
    colorList.put(1, Color.ORANGE);
    colorList.put(2, Color.RED);
    colorList.put(3, Color.GREEN);
    colorList.put(4, Color.YELLOW);
    colorList.put(5, Color.WHITE);
    colorList.put(6, Color.PINK);
    colorList.put(0, Color.BLUE);

    return colorList;
  }

  //constructor for the game, also calls the method to create the cells
  Flood(ArrayList<Cell> board, int numColors) {
    this.board = board;
    this.numColors = numColors;
    this.colorList = createColors();
    this.makeCellList();
    this.makeSurrounding();
  }

  //creates an arraylist of cells in each row and column
  public void makeCellList() {
    for (int row = 0; row < boardSize; row++) {
      for (int col = 0; col < boardSize; col++) {
        this.board.add(new Cell(col, row, this.colorList.get(rand.nextInt(numColors))));
      }
    }
  }

  //checks if player has more clicks left
  public boolean noClicksLeft() {
    return this.numClicks < 0;
  }

  //helper to check if the entire board is flooded which is a condition
  //for winning the game
  public boolean allFlooded() {
    boolean flood = true;
    for (Cell current : board) {
      if (!current.isFlooded) {
        flood = false;
        break;
      }
    }
    return flood;
  }

  //method to draw the scene
  public WorldScene makeScene() {
    WorldScene floodWorld = this.getEmptyScene();
    floodWorld.placeImageXY(background, (grid) / 2, (grid) / 2);

    //draws each cell in the board arrayList
    for (Cell current : board) {
      floodWorld.placeImageXY(current.drawCell(current.color), current.x * this.cellSize
          + this.cellSize / 2, current.y * this.cellSize + this.cellSize / 2);
      floodWorld.placeImageXY((new TextImage("Clicks left: " + String.valueOf(numClicks), cellSize,
          Color.WHITE)), boardSize * cellSize / 2, cellSize / 2);
    }
    return floodWorld;
  }

  //runs every time the mouse is clicked - checks for flooded cells
  //and decreases the clicks counter
  public void onMouseClicked(Posn pos) {
    //cell that is clicked on - can be clicked anywhere inside the square
    Cell clicked = this.board.get((Math.floorDiv(pos.x, cellSize))
        + (Math.floorDiv(pos.y, cellSize) * cellSize));

    //checks if the cells next to the cell are the same color and floods them
    for (Cell current : board) {
      if (current.isFlooded) {
        current.color = clicked.color;
        if (current.left != null && current.left.color.equals(clicked.color)) {
          current.left.isFlooded = true;
        }
        if (current.top != null && current.top.color.equals(clicked.color)) {
          current.top.isFlooded = true;
        }
        if (current.bottom != null && current.bottom.color.equals(clicked.color)) {
          current.bottom.isFlooded = true;
        }
        if (current.right != null && current.right.color.equals(clicked.color)) {
          current.right.isFlooded = true;
        }
      }
    }

    //numClicks counter
    this.numClicks -= 1;

  }

  //sets the surrounding cells to top, bottom, left, right. if there is no
  //surrounding, it remains null
  public void makeSurrounding() {
    board.get(0).isFlooded = true;

    //goes through each cell in the arrayList
    for (int i = 0; i <= ((boardSize * boardSize) - 1); i++) {
      //sets the left
      if (board.get(i).x > 0) {
        board.get(i).left = board.get(i - 1);
      }
      else {
        board.get(i).left = null;
      }

      //sets the top
      if (board.get(i).y > 0) {
        board.get(i).top = board.get(i - boardSize);
      }
      else {
        board.get(i).top = null;
      }

      //sets the right
      if (board.get(i).x < boardSize - 1) {
        board.get(i).right = board.get(i + 1);
      }
      else {
        board.get(i).right = null;
      }

      //sets the left
      if (board.get(i).y < boardSize - 1) {
        board.get(i).bottom = (board.get(i + boardSize));
      }
      else {
        board.get(i).bottom = null;
      }
    }
  }

  //end of the world - either a win or a lose screen is displayed
  public WorldEnd worldEnds() {
    if (this.allFlooded() && (!this.noClicksLeft())) {
      return new WorldEnd(true, this.makeFinalScene("you win!"));
    }
    else {
      new WorldEnd(false, this.makeScene());
    }
    if (this.noClicksLeft()) {
      return new WorldEnd(true, this.makeFinalScene("game over! no clicks left"));
    }
    return new WorldEnd(false, this.makeScene());
  }

  //final scene for when the game ends
  public WorldScene makeFinalScene(String str) {
    WorldScene floodWorld = this.getEmptyScene();
    floodWorld.placeImageXY(background, boardSize * cellSize / 2, boardSize * cellSize / 2);
    floodWorld.placeImageXY((new TextImage(str, cellSize, Color.RED)), boardSize * cellSize / 2,
        boardSize * cellSize / 2);
    return floodWorld;
  }

  //resets the game when r is pressed
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      new Flood(this.board, this.numColors);
      this.numClicks = 25;
      this.makeScene();
    }
  }
}
