package edu.stanford.nlp.sempre.interactive.actions;

import java.time.LocalDateTime;

// Individual items with some properties
public abstract class Item {
  public abstract void update(String rel, Object value, LocalDateTime datetime);
  public abstract Object get(String rel);
	// TODO Auto-generated method stub  
  
  // added
	public void move(String rel, Object value, LocalDateTime datetime) {}
	public abstract void add(String rel, Object value);
}