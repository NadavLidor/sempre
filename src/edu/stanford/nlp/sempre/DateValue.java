package edu.stanford.nlp.sempre;

import fig.basic.LispTree;
import fig.basic.LogInfo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class DateValue extends Value {
  public final int year;
  public final int month;
  public final int day;

  // Format: YYYY-MM-DD (from Freebase).
  // Return null if it's not a valid date string.
  public static DateValue parseDateValue(String dateStr) {
  	LogInfo.log("dateStr: " + dateStr);
  	
//  	if (dateStr.charAt(5) == 'W') return null; // Nadav: don't handle week days 
	    if (dateStr.equals("PRESENT_REF")) return null;
	    if (dateStr.startsWith("OFFSET P")) { //OFFSET P1D "tomorrow"
	    	LocalDateTime d = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
	  		Character unit = dateStr.charAt(dateStr.length() - 1);
	  		int value = Integer.parseInt(dateStr.substring(8, dateStr.length() - 1));
	  		if (unit.equals('M')) // coreNLP gives M to both months and minutes
					d = d.plusHours(value);
	  		else if (unit.equals('H'))
	  			d = d.plusHours(value);
	  		else if (unit.equals('D'))
					d = d.plusDays(value);
	  		else if (unit.equals('W'))
					d = d.plusDays(7 * value);
	  		else if (unit.equals('Y'))
					d = d.plusYears(value);
	    	return new DateValue(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
	    }
	    if (dateStr.startsWith("THIS P1D")) { // today
	    	LocalDateTime d = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
	    	return new DateValue(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
	    }
	    if (dateStr.startsWith("THIS")) dateStr.substring(5, dateStr.length());
	    if (dateStr.startsWith("XXXX-WXX-")) { // day of week
	    	LocalDateTime d = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
	  		int weekday = Integer.parseInt(dateStr.substring(9, 10));
	  		d = d.plusDays(weekday - d.getDayOfWeek().getValue());
	  		if (dateStr.endsWith("W")) {
	  			int week = Integer.parseInt(dateStr.substring(19, dateStr.length() - 1));
	  			d = d.plusDays(7 * week);
	  		}
	    	return new DateValue(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
	    }
    
    
    //XXXX-WXX-5 "friday"
    //THIS XXXX-WXX-5 "this friday"
    //XXXX-WXX-5 OFFSET P1W "next friday"
    //THIS P1D "today"
    //OFFSET P1D "tomorrow" 
    //XXXX-WXX-5 OFFSET P1W

    // We don't handle the following things:
    //   - "30 A.D" since its value is "+0030"
    //   - "Dec 20, 2009 10:04am" since its value is "2009-12-20T10:04"
    int year = -1, month = -1, day = -1;
    boolean isBC = dateStr.startsWith("-");
    if (isBC) dateStr = dateStr.substring(1);

    // Ignore time
    int t = dateStr.indexOf('T');
    if (t != -1) dateStr = dateStr.substring(0, t);

    String[] dateParts;

    if (dateStr.indexOf('T') != -1)
      dateStr = dateStr.substring(0, dateStr.indexOf('T'));

    dateParts = dateStr.split("-");
    if (dateParts.length > 3)
      throw new RuntimeException("Date has more than 3 parts: " + dateStr);

    if (dateParts.length >= 1) year = parseIntRobust(dateParts[0]) * (isBC ? -1 : 1);
    if (dateParts.length >= 2) month = parseIntRobust(dateParts[1]);
    if (dateParts.length >= 3) day = parseIntRobust(dateParts[2]);

    return new DateValue(year, month, day);
  }

  private static int parseIntRobust(String i) {
    int val;
    try {
      val = Integer.parseInt(i);
    } catch (NumberFormatException ex) {
      val = -1;
    }
    return val;
  }

  public static DateValue now() {
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);
    int day = cal.get(Calendar.DAY_OF_MONTH);
    return new DateValue(year, month, day);
  }

  public DateValue(int year, int month, int day) {
    this.year = year;
    this.month = month;
    this.day = day;
  }

  public DateValue(LispTree tree) {
    this.year = Integer.valueOf(tree.child(1).value);
    this.month = Integer.valueOf(tree.child(2).value);
    this.day = Integer.valueOf(tree.child(3).value);
  }

  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    tree.addChild("date");
    tree.addChild(String.valueOf(year));
    tree.addChild(String.valueOf(month));
    tree.addChild(String.valueOf(day));
    return tree;
  }

  @Override public String sortString() { return "" + year + "/" + month + "/" + day; }
  public String isoString() {
    return "" + (year == -1 ? "xxxx" : String.format("%04d", year))
        + "-" + (month == -1 ? "xx" : String.format("%02d", month))
        + "-" + (day == -1 ? "xx" : String.format("%02d", day));
  }

  @Override public int hashCode() {
    int hash = 0x7ed55d16;
    hash = hash * 0xd3a2646c + year;
    hash = hash * 0xd3a2646c + month;
    hash = hash * 0xd3a2646c + day;
    return hash;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DateValue that = (DateValue) o;
    if (this.year != that.year) return false;
    if (this.month != that.month) return false;
    if (this.day != that.day) return false;
    return true;
  }
  
  // return true if this DateValue is after other DateValue
  public boolean isAfter(DateValue other) {
  	// if value is -1 in either, disregard field
  	int this_year = this.year; int this_month = this.month; int this_day = this.day;
  	int other_year = other.year; int other_month = other.month; int other_day = other.day;
  	if (this_year == -1 || other_year == -1) {this_year = -1; other_year = -1;}
  	if (this_month == -1 || other_month == -1) {this_month = -1; other_month = -1;}
  	if (this_day == -1 || other_day == -1) {this_day = -1; other_day = -1;}
  	LogInfo.log((this_year > other_year) || (this_year == other_year && this_month > other_month) || (this_year == other_year && this_month == other_month && this_day > other_day)); // TODO
  	return ((this_year > other_year) || (this_year == other_year && this_month > other_month) || (this_year == other_year && this_month == other_month && this_day > other_day));
  }
  // return true if this DateValue is after other DateValue
  public boolean isBefore(DateValue other) {
  	// if value is -1 in either, disregard field
  	int this_year = this.year; int this_month = this.month; int this_day = this.day;
  	int other_year = other.year; int other_month = other.month; int other_day = other.day;
  	if (this_year == -1 || other_year == -1) {this_year = -1; other_year = -1;}
  	if (this_month == -1 || other_month == -1) {this_month = -1; other_month = -1;}
  	if (this_day == -1 || other_day == -1) {this_day = -1; other_day = -1;}
  	LogInfo.log((this_year < other_year) || (this_year == other_year && this_month < other_month) || (this_year == other_year && this_month == other_month && this_day < other_day)); // TODO
  	return ((this_year < other_year) || (this_year == other_year && this_month < other_month) || (this_year == other_year && this_month == other_month && this_day < other_day));
  }
}
