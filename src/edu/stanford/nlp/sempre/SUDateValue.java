package edu.stanford.nlp.sempre;

import fig.basic.LispTree;

public class SUDateValue extends Value {
	
	public final String date;

  public static SUDateValue parseDateValue(String dateStr) {
  	if (dateStr == null || dateStr.length() < 1) return null;
    return new SUDateValue(dateStr);
  }

  public SUDateValue(String date) {
    this.date = date;
  }
  
  public SUDateValue(LispTree tree) {
    this.date = tree.child(1).value;
  }

  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    tree.addChild("SUdate");
    tree.addChild(this.date);
    return tree;
  }

  @Override public int hashCode() {
    return this.date.hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SUDateValue that = (SUDateValue) o;
    return this.date.equals(that.date);
  }
}
