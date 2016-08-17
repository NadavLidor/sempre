package edu.stanford.nlp.sempre;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import fig.basic.LispTree;
import fig.basic.LogInfo;

/**
 * Created by Nadav Lidor on 08/16/15.
 * Value for representing datetime
 */
public class DateTimeValue extends Value {

		public final LocalDateTime datetime;

  // timeStr format is THH:MM:SS (M and S optional)
  public static DateTimeValue parseDateTimeValue(String timeStr) {
	
  	LocalDateTime d = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
  
  	if (timeStr.charAt(0) != 'T') { // dateTime
  		String dateStr = timeStr.substring(0, timeStr.indexOf('T'));
  		
  		String maybeYear = dateStr.substring(0, 4);
    	if (!maybeYear.equals("XXXX")) {
    		try {
    			d = d.withYear(Integer.parseInt(maybeYear));
      	} catch (NumberFormatException e) {} // do nothing on exception
    	}
    	
    	// check if weekday
    	if (dateStr.charAt(5) == 'W') {
    		try {
    			int weekday = Character.getNumericValue(dateStr.charAt(5));
    			d = d.plusDays(d.getDayOfWeek().getValue() - weekday);
      	} catch (NumberFormatException e) {} // do nothing on exception
    	}

    	// else check if month / day
    	else {      	
      	String maybeMonth = dateStr.substring(5, 7);
      	if (!maybeMonth.equals("XX")) {
      		try {
      			d = d.withMonth(Integer.parseInt(maybeMonth));
        	} catch (NumberFormatException e) {} // do nothing on exception
      	}
      	String maybeDay = dateStr.substring(8, 10);
      	if (!maybeDay.equals("XX")) {
      		try {
      			d = d.withDayOfMonth(Integer.parseInt(maybeDay));
        	} catch (NumberFormatException e) {} // do nothing on exception
      	}
    	} 	
  	}
  	
  	// extract time
  	String hourStr = timeStr.substring(timeStr.indexOf('T'), timeStr.length());
  	
  	try {
  		d = d.withHour(Integer.parseInt(hourStr.substring(1, 3)));
  	} catch (NumberFormatException e) {} // do nothing on exception
  	
  	
  	// extract minute
  	if (hourStr.length() >= 5) {
    	try {
    		d = d.withMinute(Integer.parseInt(hourStr.substring(4, 6)));
    	} catch (NumberFormatException e) {} // do nothing on exception
  	}
		
		return new DateTimeValue(d);
	
  }
  
  public DateTimeValue(LocalDateTime d) {
    this.datetime = d;
  }

  public DateTimeValue(LispTree tree) {
  	
  	this.datetime  = LocalDateTime.parse(tree.child(1).toString());
  	
  }

  @Override
  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    tree.addChild("datetime");
    tree.addChild(datetime.toString());
    return tree;
  }

  @Override
  public boolean equals(Object o) {
  	
//  	LogInfo.log("here");
//  	LogInfo.log(this.datetime);
  	
    if (this == o) return true;

    if (o != null) {
    	if (getClass() == o.getClass()) { // if o is DateTimeValue
//    		LogInfo.log("here2");
    		
        DateTimeValue otherDatetime = (DateTimeValue) o;
//        LogInfo.log(otherDatetime.datetime);
//        LogInfo.log(this.datetime.equals(otherDatetime.datetime));
        return this.datetime.equals(otherDatetime.datetime);
    	}
    	if (o instanceof LocalDateTime) { // if o is LocalDateTime
//    		LogInfo.log("here3");
        LocalDateTime otherDatetime = (LocalDateTime) o;
//        LogInfo.log(otherDatetime);
        return this.datetime.equals(otherDatetime);    		
    	}
    }
    return false;
    
  }

  @Override
  public int hashCode() {
    return datetime.hashCode();
  }
  
  // return true if this TimeValue is after other TimeValue
  public boolean isAfter(DateTimeValue other) {
  	return this.datetime.isAfter(other.datetime);
  }
  
  // return true if this TimeValue is before other TimeValue
  public boolean isBefore(DateTimeValue other) {
  	return this.datetime.isBefore(other.datetime); 
  }
  

}
