package com.mmyzd.llor.displaymode.datatype;

public class TextureIndex {

  private final int row;
  private final int column;

  public TextureIndex(int row, int column) {
    this.row = row;
    this.column = column;
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }
}
