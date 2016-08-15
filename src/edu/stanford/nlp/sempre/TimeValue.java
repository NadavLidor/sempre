package edu.stanford.nlp.sempre;

import fig.basic.LispTree;
import fig.basic.LogInfo;

/**
 * Created by joberant on 1/23/15.
 * Value for representing time
 */
public class TimeValue extends Value {

  public final int hour;
  public final int minute;

  // timeStr format is THH:MM:SS (M and S optional)
  public static TimeValue parseTimeValue(String timeStr) {
	
  	//extract hour 
  	int hour = -1;
  	int minute = 0;
  
//  	if (timeStr.charAt(0) != 'T') LogInfo.log("BAD timeStr: " + timeStr + " returning null");
  	if (timeStr.charAt(0) != 'T') return null; // Don't handle dateTime
  	
  	
  	String hourStr = timeStr.substring(timeStr.indexOf('T'), timeStr.length());
  	LogInfo.log("timeStr: " + timeStr);
  	
  	try {
  		hour = Integer.parseInt(hourStr.substring(1, 3));
  	} catch (NumberFormatException e) {} // do nothing on exception
  	
  	
  	// extract minute
  	if (timeStr.length() >= 5) {
    	try {
    		minute = Integer.parseInt(hourStr.substring(4, 6));
    	} catch (NumberFormatException e) {} // do nothing on exception
  	}
		
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
