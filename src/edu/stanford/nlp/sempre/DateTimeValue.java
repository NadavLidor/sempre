package edu.stanford.nlp.sempre;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.sempre.interactive.actions.EventsWorld;
import fig.basic.LispTree;
import fig.basic.LogInfo;

/**
 * Created by Nadav Lidor on 08/16/15.
 * Value for representing datetime
 */
public class DateTimeValue extends Value {

	public final LocalDateTime datetime;

	// format 2000-08-18T15:00, with possible Xs
  public static DateTimeValue parseDateTimeValue(String timeStr) {
//  	LogInfo.log("DateTimeValue dateStr: " + timeStr);
//  	LocalDateTime d = LocalDateTime.now(ZoneId.of("UTC+00:00"));
//  	LocalDateTime d = EventsWorld.calendarTime(); //TODO
  	LocalDateTime d = LocalDateTime.now(); //TODO
  	if (d.getMinute() > 30) {
  		d = d.plusMinutes(30);
  		d = d.truncatedTo(ChronoUnit.HOURS);
  	}
  	else if (d.getMinute() > 0) {
  		d = d.withMinute(30);
  		d = d.truncatedTo(ChronoUnit.MINUTES);
  	}
  	
  	
  	Pattern PATTERN = Pattern.compile("(T\\d+)");
  	
  	String timeS = "";
  	int Tindex = -1;
  	Matcher m = PATTERN.matcher(timeStr);
  	if (m.find()) {
  	    timeS = m.group(1);
  	    Tindex = timeStr.indexOf(timeS);
  	}
  	
  	if (Tindex > 0) { // only handle datetimes

  		String dateStr = timeStr.substring(0, Tindex);
  		// XXXX-WXX-3 OFFSET P1W INTERSECT T15

  		if (timeStr.contains("OFFSET P")) {
  			int index = timeStr.indexOf("OFFSET P");
  			int end = ((timeStr.length() < index + 13) ? timeStr.length() : timeStr.indexOf(' ', index + 8));
  			String offset = timeStr.substring(index, end);
  			end--; // point to unit
	  		
  			if (offset.charAt(8) == 'T') { //OFFSET PT3H "three hours from now"
		  		Character unit = offset.charAt(end);
		  		int value = Integer.parseInt(offset.substring(9, end));
		  		if (unit.equals('M')) // 
						d = d.plusMinutes(value);
		  		else if (unit.equals('H'))
		  			d = d.plusHours(value);
		  		return new DateTimeValue(d); // skip time extraction
	  		}
	  		
	  		else { 	//OFFSET P1D "tomorrow" 
	  						//XXXX-WXX-3 OFFSET P1W INTERSECT T15 "next wed at 3pm"
		  		Character unit = offset.charAt(end);
		  		int value = Integer.parseInt(offset.substring(8, end));
		  		if (unit.equals('M')) // coreNLP gives M to both months and minutes
						d = d.plusMinutes(value);
		  		else if (unit.equals('H'))
		  			d = d.plusHours(value);
		  		else if (unit.equals('D'))
						d = d.plusDays(value);
		  		else if (unit.equals('W'))
						d = d.plusDays(7 * value);
		  		else if (unit.equals('Y'))
						d = d.plusYears(value);
	  		}
  		}
  		else if (dateStr.startsWith("THIS P1D")) {
  			// do nothing, today
  		}
  		else if (dateStr.startsWith("THIS")) 
	    	dateStr = dateStr.substring(5, dateStr.length());
  		else if (dateStr.startsWith("XXXX-WXX-")) { // day of week
	  		int weekday = Integer.parseInt(dateStr.substring(9, 10));
	  		d = d.plusDays(weekday - d.getDayOfWeek().getValue());
	  		if (dateStr.endsWith("W")) {
	  			int week = Integer.parseInt(dateStr.substring(19, dateStr.length() - 1));
	  			d = d.plusDays(7 * week);
	  		}
	    }
  		
  		else {
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
	  	String hourStr = timeStr.substring(Tindex, timeStr.length());
//	  	LogInfo.log("DateTimeValue.hourStr: " + hourStr);
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
  	return null;
	
  }
  
  public static DateTimeValue parseSUDateTimeValue(String timeStr, LocalDateTime currentDatetime) {
	LocalDateTime d = currentDatetime;
	if (d.getMinute() > 30) {
		d = d.plusMinutes(30);
		d = d.truncatedTo(ChronoUnit.HOURS);
	}
	else if (d.getMinute() > 0) {
		d = d.withMinute(30);
		d = d.truncatedTo(ChronoUnit.MINUTES);
	}
	
	
	Pattern PATTERN = Pattern.compile("(T\\d+)");
	
	String timeS = "";
	int Tindex = -1;
	Matcher m = PATTERN.matcher(timeStr);
	if (m.find()) {
	    timeS = m.group(1);
	    Tindex = timeStr.indexOf(timeS);
	}
	
	if (Tindex > 0) { // only handle datetimes

		String dateStr = timeStr.substring(0, Tindex);
		// XXXX-WXX-3 OFFSET P1W INTERSECT T15

		if (timeStr.contains("OFFSET P")) {
			int index = timeStr.indexOf("OFFSET P");
			int end = ((timeStr.length() < index + 13) ? timeStr.length() : timeStr.indexOf(' ', index + 8));
			String offset = timeStr.substring(index, end);
			end--; // point to unit
  		
			if (offset.charAt(8) == 'T') { //OFFSET PT3H "three hours from now"
	  		Character unit = offset.charAt(end);
	  		int value = Integer.parseInt(offset.substring(9, end));
	  		if (unit.equals('M')) // 
					d = d.plusMinutes(value);
	  		else if (unit.equals('H'))
	  			d = d.plusHours(value);
	  		return new DateTimeValue(d); // skip time extraction
  		}
  		
  		else { 	//OFFSET P1D "tomorrow" 
  						//XXXX-WXX-3 OFFSET P1W INTERSECT T15 "next wed at 3pm"
	  		Character unit = offset.charAt(end);
	  		int value = Integer.parseInt(offset.substring(8, end));
	  		if (unit.equals('M')) // coreNLP gives M to both months and minutes
					d = d.plusMinutes(value);
	  		else if (unit.equals('H'))
	  			d = d.plusHours(value);
	  		else if (unit.equals('D'))
					d = d.plusDays(value);
	  		else if (unit.equals('W'))
					d = d.plusDays(7 * value);
	  		else if (unit.equals('Y'))
					d = d.plusYears(value);
  		}
		}
		else if (dateStr.startsWith("THIS P1D")) {
			// do nothing, today
		}
		else if (dateStr.startsWith("THIS")) 
    	dateStr = dateStr.substring(5, dateStr.length());
		else if (dateStr.startsWith("XXXX-WXX-")) { // day of week
  		int weekday = Integer.parseInt(dateStr.substring(9, 10));
  		d = d.plusDays(weekday - d.getDayOfWeek().getValue());
  		if (dateStr.endsWith("W")) {
  			int week = Integer.parseInt(dateStr.substring(19, dateStr.length() - 1));
  			d = d.plusDays(7 * week);
  		}
    }
		
		else {
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
  	String hourStr = timeStr.substring(Tindex, timeStr.length());
//  	LogInfo.log("DateTimeValue.hourStr: " + hourStr);
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
	return null;

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
  	
    if (this == o) return true;

    if (o != null) {
    	if (getClass() == o.getClass()) { // if o is DateTimeValue
    		
        DateTimeValue otherDatetime = (DateTimeValue) o;
        return this.datetime.equals(otherDatetime.datetime);
    	}
    	if (o instanceof LocalDateTime) { // if o is LocalDateTime
        LocalDateTime otherDatetime = (LocalDateTime) o;
        return this.datetime.equals(otherDatetime);    		
    	}
    }
    return false;
    
  }
  
  public static DateTimeValue now() {
//  	LocalDateTime d = EventsWorld.calendarTime();
  	LocalDateTime d = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
    return new DateTimeValue(d);
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
