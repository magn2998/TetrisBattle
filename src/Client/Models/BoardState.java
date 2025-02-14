package Client.Models;


import Client.GameSession.ProducerPackageHandler;
import Client.Utility.Utils;
import common.Constants;
import javafx.scene.paint.Color;

import java.util.BitSet;
import java.util.Random;

public class BoardState {
  private ProducerPackageHandler packageHandler;
  private Mino[] board;
  private int attackQueue = 0;

  public BoardState(int size) { // Size is equal to the amount of cells in the tetris grid
    this.board = new Mino[size];
  }

  public Mino[] getBoard() {
    return board;
  }

  public void addPackageHandler(ProducerPackageHandler packageHandler) { this.packageHandler = packageHandler; }

  public void setBoard(Mino[] board) {
    this.board = board;
  }

  public void insertTetromino(Tetromino tetromino) {
    removeOrInsertTetromino(tetromino, true, false);
  }

  public void removeTetromino(Tetromino tetromino) {
    removeOrInsertTetromino(tetromino, false, false);
  }

  public void placeTetromino(Tetromino tetromino) {
    removeOrInsertTetromino(tetromino, true, true);
  }

  private void removeOrInsertTetromino(Tetromino tetromino, boolean insertOrRemove, boolean isPlaced) { // true => insert
    int[] currentState = tetromino.getCurrentRotation();
    int posX = tetromino.posX;
    int posY = tetromino.posY;

    for(int x = 0; x < 4; x++) {
      for(int y = 0; y < 4; y++) {
        int index = y*4+x;
        int boardIndex = (posY + y)*10 + (posX+x);
        if(currentState[index] == 1 && boardIndex >= 0 && boardIndex < 200) {
          board[boardIndex] = insertOrRemove ? new Mino(posX+x, posY+y, tetromino.color, isPlaced) : null;

          if(this.packageHandler != null && !tetromino.isGhost)
            this.packageHandler.updateDelta(this.board, boardIndex);
        }
      }
    }
  }

  public boolean legalPosition(Tetromino tetromino, int offsetX, int offsetY) {
    return legalPosition(tetromino.getCurrentRotation(), tetromino.posX+offsetX, tetromino.posY+offsetY);
  }

  public boolean legalPosition(int[] state, int posX, int posY) {
    for(int x = 0; x < 4; x++) {
      for(int y = 0; y < 4; y++) {
        int index = y*4+x;
        int boardIndex = (posY + y)*10 + (posX+x);
        boolean indexIsOutOfBounds = (posY+y) >= 20 || (posY+y<0) || (posX+x) >= 10 || (posX+x) < 0; // Is the position within the board?
        boolean indexIsOutOfBoundsButNotTop = (posY+y) >= 20 || (posX+x) >= 10 || (posX+x) < 0; // Is the position within the board, but ignoring the top (Used when spawning, since the tetrominos can spawn with their upper half over the top, where the user should be able to move it)
        boolean overlapsAnotherBlock = state[index] == 1 && !indexIsOutOfBounds && this.board[boardIndex] != null && this.board[boardIndex].isPlaced; // Is there a mino in the way?
        boolean tetrominoGoesOutOfBound = state[index] == 1 && indexIsOutOfBoundsButNotTop; // Is the tetromino about to go out of the board?
        if(overlapsAnotherBlock || tetrominoGoesOutOfBound) {
          return false;
        }
      }
    }
    return true;
  }

  // Returns the index of which wall kick rotation is legal. -1 means no legal rotation was found.
  public int getLegalRotation(int state[], int[][] wallKickData, int posX, int posY ) {
    for(int i = 0; i < wallKickData.length; i++) {
      if(legalPosition(state, posX+wallKickData[i][0], posY+wallKickData[i][1])) {
        return i;
      }
    }
    return -1;
  }

  // When using space to drop the tetromino
  public void dropTetromino(Tetromino tetromino) {
    int newY = getLowestLegalYcoord(tetromino);
    removeTetromino(tetromino);
    tetromino.posY = newY;
    placeTetromino(tetromino);
  }

  public int getLowestLegalYcoord(Tetromino tetromino) {
    int[] state = tetromino.getCurrentRotation();
    int posX = tetromino.posX;
    int posY = tetromino.posY;

    for(int i = posY+1; i <= 20; i++) {
      if(!legalPosition(state, posX, i)) {
        return i -1;
      }
    }
    return -1;
  }

  // Removes the rows that are filled. It will check row at posY and the following three rows, since a tetromino can, at most, influence 4 rows.
  public int removeFilledRows(int posY) {
    int numRowsRemoved = 0;
    for(int y = posY; y < posY+4 && y < 20; y++) {
      if(y < 0) continue;

      for(int x = 0; x < 10; x++) {
        int index = y*10+x;

        if(board[index] == null) {
          break;
        } else if(x == 9) {
          removeRow(y);
          numRowsRemoved++;
        }
      }
    }
    return numRowsRemoved;
  }

  public void removeRow(int y) {
    for(int index = y*10+9; index >= 0; index--) {
      if(index > 9) {
        board[index] = board[index-10];
      } else {
        board[index] = null;
      }
      if(this.packageHandler != null)
        this.packageHandler.updateDelta(this.board, index);
    }
  }

  public void addRows(int amount) {
    if(amount <= 0) return;
    int rndHolePlacement = (new Random()).nextInt(9);
    for(int index = 0; index < 200; index++) {
      if(index < 200-amount*10) {
        this.board[index] = this.board[index+amount*10];
      } else {
        if(index%10 == rndHolePlacement) {
          this.board[index] = null;
        } else {
          this.board[index] = new Mino(index, Constants.receivedLineColor, true);
        }
      }
    }
  }

  public void addToAttackQueue(int amount) {
    this.attackQueue += amount;
  }

  public int getAttackQueue() {
    return this.attackQueue;
  }

  public void resetAttackQueue() {
    this.attackQueue = 0;
  }

  public String toString() {
    StringBuilder s = new StringBuilder();
    for(int index = 0; index < 200; index++) {
      if(index % 10 == 0 && index != 0) {
        s.append("\n");
      }

      String os = System.getProperty("os.name");
      if(board[index] == null) {
        if (os.equals("Mac OS X"))
          s.append("⬜");
        else
          s.append("🟨");
      } else {
        if (os.equals("Windows 10"))
        {
          s.append("🟥");
        } else {
          if (board[index].color.equals(Color.DARKSLATEBLUE)) {
            s.append("🟦");
          } else if(board[index].color.equals(Color.CYAN)) {
            s.append("🟪");
          } else if(board[index].color.equals(Color.ORANGE)) {
            s.append("🟧");
          } else if(board[index].color.equals(Color.YELLOW)) {
            s.append("🟨");
          } else if(board[index].color.equals(Color.GREEN)) {
            s.append("🟩");
          } else if(board[index].color.equals(Color.DARKMAGENTA)) {
            s.append("🟫");
          } else if(board[index].color.equals(Color.FIREBRICK)) {
            s.append("🟥");
          } else {
            s.append("⬛");
          }
        }
      }
      s.append(" ");
    }
    return s.toString();
  }

  public BitSet toBitArray() {
    BitSet bitArray = new BitSet(200*3);

    for(int i = 0; i < 200; i++) {
      if(board[i] == null) {
        bitArray.set(i*3, false); bitArray.set(i*3+1, false); bitArray.set(i*3+2, false);
      } else if (board[i].color.equals(Color.DARKSLATEBLUE)) {
        bitArray.set(i*3, false); bitArray.set(i*3+1, false); bitArray.set(i*3+2, true);
      } else if(board[i].color.equals(Color.CYAN)) {
        bitArray.set(i*3, false); bitArray.set(i*3+1, true); bitArray.set(i*3+2, false);
      } else if(board[i].color.equals(Color.ORANGE)) {
        bitArray.set(i*3, false); bitArray.set(i*3+1, true); bitArray.set(i*3+2, true);
      } else if(board[i].color.equals(Color.YELLOW) || board[i].color.equals(Constants.receivedLineColor)) {
        bitArray.set(i*3, true); bitArray.set(i*3+1, false); bitArray.set(i*3+2, false);
      } else if(board[i].color.equals(Color.GREEN)) {
        bitArray.set(i*3, true); bitArray.set(i*3+1, false); bitArray.set(i*3+2, true);
      } else if(board[i].color.equals(Color.DARKMAGENTA)) {
        bitArray.set(i*3, true); bitArray.set(i*3+1, true); bitArray.set(i*3+2, false);
      } else if(board[i].color.equals(Color.FIREBRICK)) {
        bitArray.set(i*3, true); bitArray.set(i*3+1, true); bitArray.set(i*3+2, true);
      }
    }

    return bitArray;
  }

  public void setBoardStateFromBitArray(BitSet bitArray) {
    System.out.println("bits=" + bitArray.length());
    for(int index = 0; index < bitArray.length(); index += 3) {
      int indexInBoard = index/3;
      boolean leftBit = bitArray.get(index);
      boolean middleBit = bitArray.get(index+1);
      boolean rightBit = bitArray.get(index+2);

      if(!leftBit && !middleBit && !rightBit) {
        board[indexInBoard] = null;
      } else if (!leftBit && !middleBit && rightBit) {
        board[indexInBoard] = new Mino(indexInBoard, Color.DARKSLATEBLUE, true);
      } else if(!leftBit && middleBit && !rightBit) {
        board[indexInBoard] = new Mino(indexInBoard, Color.CYAN, true);
      } else if(!leftBit && middleBit && rightBit) {
        board[indexInBoard] = new Mino(indexInBoard, Color.ORANGE, true);
      } else if(leftBit && !middleBit && !rightBit) {
        board[indexInBoard] = new Mino(indexInBoard, Color.YELLOW, true);
      } else if(leftBit && !middleBit && rightBit) {
        board[indexInBoard] = new Mino(indexInBoard, Color.GREEN, true);
      } else if(leftBit && middleBit && !rightBit) {
        board[indexInBoard] = new Mino(indexInBoard, Color.DARKMAGENTA, true);
      } else if(leftBit && middleBit && rightBit) {
        board[indexInBoard] = new Mino(indexInBoard, Color.FIREBRICK, true);
      }
    }
  }

  public void updateBoardFromDelta(int[] delta) {
    for(int i = 0; i < delta.length; i += 2) {
      if(delta[i+1] == -1) {
        this.board[delta[i]] = null;
      } else {
        this.board[delta[i]] = new Mino(delta[i], Utils.intToColor(delta[i+1]), true);
      }
    }
  }

  public int[] getLatestDeltaAndReset() {
    return Utils.deltaHashmapToArray(this.packageHandler.retrieveAndResetDelta(this.board));
  }
}
