package edu.stanford.nlp.sempre;

import fig.basic.LispTree;

/**
 * Created by joberant on 1/23/15.
 * Value for representing time
 */
public class TimeValue extends Value {

  public final int hour;
  public final int minute;

  // TODO Nadav
  public static TimeValue parseTimeValue(String timeStr) {
	
		// [3:00|03:00|3] pm
		// 15:00
		  
		int hour = 0;
		int minute = 0;
		
		return new TimeValue(hour, minute);
	
  }
  
  public TimeValue(int hour, int minute) {
    if (hour > 23 || hour < 0) throw new RuntimeException("Illegal hour: " + hour);
    if (minute > 59 || minute < 0) throw new RuntimeException("Illegal minute: " + minute);
    this.hour = hour;
    this.minute = minute;
  }

  public TimeValue(LispTree tree) {
    this.hour = Integer.valueOf(tree.child(1).value);
    this.minute = Integer.valueOf(tree.child(2).value);
  }

  @Override
  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    tree.addChild("time");
    tree.addChild(String.valueOf(hour));
    tree.addChild(String.valueOf(minute));
    return tree;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TimeValue timeValue = (TimeValue) o;
    if (hour != timeValue.hour) return false;
    if (minute != timeValue.minute) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = hour;
    result = 31 * result + minute;
    return result;
  }
  
  // return true if this TimeValue is after other TimeValue
  public boolean isAfter(TimeValue other) {
  	return ((this.hour > other.hour) || (this.hour == other.hour && this.minute > other.minute));
  }
  
  // return true if this TimeValue is before other TimeValue
  public boolean isBefore(TimeValue other) {
  	return ((this.hour < other.hour) || (this.hour == other.hour && this.minute < other.minute)); 
  }
  

}
