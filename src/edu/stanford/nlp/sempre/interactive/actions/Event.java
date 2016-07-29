package edu.stanford.nlp.sempre.interactive.actions;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.collections.Lists;

import edu.stanford.nlp.sempre.DateValue;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.NumberValue;
import edu.stanford.nlp.sempre.TimeValue;
import fig.basic.LogInfo;

class Person {
	 public String first;
	 public String last;
	 public String email;
	}

//individual stacks
public class  Event extends Item {
	
	public String title;
  public String location;
  public LocalDateTime start;
  public LocalDateTime end;
  public List<Integer> repeats; // [0,6] days of week, [7] monthly, [8] yearly
  public HashSet<Person> guests;
  public Set<String> names;
	
  
  public Event() {
    this.title = "meeting";
		this.location = "none";
		this.repeats = new ArrayList<Integer>(Collections.nCopies(9, 0));
		this.guests = new HashSet<Person>();
		this.names = new HashSet<>();
			
		this.start = LocalDateTime.now();
		if (this.start.getMinute() > 30) {
			this.start = this.start.plusHours(1);
			this.start = this.start.truncatedTo(ChronoUnit.HOURS);
		}
		else {
			this.start = this.start.truncatedTo(ChronoUnit.HOURS);
			this.start = this.start.plusMinutes(30);
		}
		this.end = this.start.plusHours(1);
  }
  
  public Event(String title, String location) {
  	this();
  	this.title = title;
  	this.location = location;
  }
  
  public Event(String title, String location, LocalDateTime start, LocalDateTime end, List<Integer> repeats, HashSet<Person> guests) {
  	this();
  	this.title = title;
  	this.location = location;
  	this.start = start.plusMinutes(0); //TODO better way?
  	this.end = end.plusMinutes(0);
  	for (Integer i : repeats) i = repeats.get(i);
  	// TODO guests

  }
   
  public Event copy() { // TODO needed?
    return this.clone();
  }
  
  @Override
  public Object get(String property) {
    Object propval;
    if (property.equals("title"))
        propval = this.title;
    else if (property.equals("location"))
        propval = this.location;
    else if (property.equals("duration"))
      propval = new NumberValue(this.start.until(this.end, ChronoUnit.MINUTES), "minutes");
    else if (property.equals("start_weekday"))
        propval = this.start.getDayOfWeek().getValue();
    else if (property.equals("end_weekday"))
        propval = this.end.getDayOfWeek().getValue();
    else if (property.equals("start_date"))
      propval = new DateValue(this.start.getYear(), this.start.getMonthValue(), this.start.getDayOfMonth()); 
    else if (property.equals("end_date"))
      propval = new DateValue(this.end.getYear(), this.end.getMonthValue(), this.end.getDayOfMonth());
    else if (property.equals("start_time"))
      propval = new TimeValue(this.start.getHour(), this.start.getMinute()); 
    else if (property.equals("end_time"))
      propval = new TimeValue(this.end.getHour(), this.end.getMinute());
    else if (property.equals("start_datetime"))
      propval = this.start;
    else if (property.equals("end_datetime"))
      propval = this.end;
    else
      throw new RuntimeException("EVENT GET property " + property + " is not supported.");
    
    LogInfo.log("EVENT GET: " + propval.toString());
    return propval;
  }
  
  @Override
  public void add(String property, Object value) {
    if (property.equals("start_datetime") && value instanceof NumberValue)
    	addDateTime((NumberValue)value, "start");
    else if (property.equals("end_datetime") && value instanceof NumberValue)
    	addDateTime((NumberValue)value, "end");
    	
    //TODO continue

    else
      throw new RuntimeException("EVENT UPDATE setting property " + property + " is not supported.");
  }
  
  
  @Override
  public void update(String property, Object value) {
    if (property.equals("title") && value instanceof String)
    	this.title = (String)value;
    else if (property.equals("location") && value instanceof String)
    	this.location = (String)value;
    else if (property.equals("start_weekday") && value instanceof String)
    	updateWeekday(Utils.weekdayToInt((String)value), "start");
    else if (property.equals("end_weekday") && value instanceof String)
    	updateWeekday(Utils.weekdayToInt((String)value), "end");
    else if (property.equals("duration") && value instanceof NumberValue)
    	updateDuration((NumberValue)value);
    else if (property.equals("start_date") && value instanceof DateValue)
    	updateDate((DateValue)value, "start");
    else if (property.equals("end_date") && value instanceof DateValue)
    	updateDate((DateValue)value, "end");
    else if (property.equals("start_time") && value instanceof TimeValue)
    	updateTime((TimeValue)value, "start");
    else if (property.equals("end_time") && value instanceof TimeValue)
    	updateTime((TimeValue)value, "end");
    else if (property.equals("start_datetime") && value instanceof Set<?>)
    	updateDateTime((Set<LocalDateTime>)value, "start");
    else if (property.equals("end_datetime") && value instanceof Set<?>)
    	updateDateTime((Set<LocalDateTime>)value, "end");
    	
    //TODO continue

    else {
      LogInfo.log(value instanceof TimeValue); // TODO
      LogInfo.log(value instanceof LocalDateTime); // TODO
      LogInfo.log(value instanceof Item);
      LogInfo.log(value instanceof DateValue);
      LogInfo.log(value instanceof String);
      LogInfo.log(value instanceof Object);
      throw new RuntimeException("EVENT UPDATE setting property " + property + " is not supported.");
    }
      
  }
  
  // move is similar to update, but preserves the current duration
  @SuppressWarnings("unchecked")
	public void move(String property, Object value) {
    if (property.equals("start_weekday") && value instanceof String)
    	moveWeekday(Utils.weekdayToInt((String)value), "start");
    else if (property.equals("end_weekday") && value instanceof String)
    	moveWeekday(Utils.weekdayToInt((String)value), "end");
    else if (property.equals("start_date") && value instanceof DateValue)
    	moveDate((DateValue)value, "start");
    else if (property.equals("end_date") && value instanceof DateValue)
    	moveDate((DateValue)value, "end");
    else if (property.equals("start_time") && value instanceof TimeValue)
    	moveTime((TimeValue)value, "start");
    else if (property.equals("end_time") && value instanceof TimeValue)
    	moveTime((TimeValue)value, "end");
    else if (property.equals("start_datetime") && value instanceof Set<?>)
    	moveDateTime((Set<LocalDateTime>)value, "start");
    else if (property.equals("end_datetime") && value instanceof Set<?>)
    	moveDateTime((Set<LocalDateTime>)value, "end");
    
    
    //TODO continue
    
    else
      throw new RuntimeException("EVENT MOVE setting property " + property + " is not supported.");
  }

  public void updateTime(TimeValue time, String op) {
	  if (op.equals("start")) {
		  this.start = this.start.with(ChronoField.HOUR_OF_DAY, time.hour);
		  this.start = this.start.with(ChronoField.MINUTE_OF_HOUR, time.minute);
	  }
	  else if (op.equals("end")) {
		  this.end = this.end.with(ChronoField.HOUR_OF_DAY, time.hour);
		  this.end = this.end.with(ChronoField.MINUTE_OF_HOUR, time.minute);
	  }
  }
  
  public void updateDateTime(Set<LocalDateTime> value, String op) {
  	
  	LocalDateTime sample = LocalDateTime.now();
  	for (LocalDateTime i : value) {sample = i; break;}
  	
	  if (op.equals("start")) this.start = sample;
	  else if (op.equals("end")) this.end = sample;
  }
  
  public void addDateTime(NumberValue time, String op) {
	  if (op.equals("start")) {
	  	if (time.unit.equals("minutes")) this.start = this.start.plusMinutes((int)time.value);
	  	else if (time.unit.equals("hours")) this.start = this.start.plusHours((int)time.value);
	  	
	  	//TODO more units
	  }
	  else if (op.equals("end")) {
	  	if (time.unit.equals("minutes")) this.end = this.end.plusMinutes((int)time.value);
	  	else if (time.unit.equals("hours")) this.end = this.end.plusHours((int)time.value);
	  }
  }
  
  public void moveDateTime(Set<LocalDateTime> value, String op) {
  	long duration = this.start.until(this.end, ChronoUnit.MINUTES);
  	LogInfo.log("moveDateTime EVENT: " + value.toString());
  	LocalDateTime sample = LocalDateTime.now();
  	for (LocalDateTime i : value) {sample = i; break;}
  	
	  if (op.equals("start")) {
	  	this.start = sample;
	  	this.end = this.start.plusMinutes(duration);
	  }
	  else if (op.equals("end")) {
	  	this.end = sample;
	  	this.start = this.end.plusMinutes(-duration);
	  }
  }
  
  public void moveTime(TimeValue time, String op) {
  	long duration = this.start.until(this.end, ChronoUnit.MINUTES);
	  if (op.equals("start")) {
		  this.start = this.start.with(ChronoField.HOUR_OF_DAY, time.hour);
		  this.start = this.start.with(ChronoField.MINUTE_OF_HOUR, time.minute);
		  this.end = this.start.plusMinutes(duration);
	  }
	  else if (op.equals("end")) {
		  this.end = this.end.with(ChronoField.HOUR_OF_DAY, time.hour);
		  this.end = this.end.with(ChronoField.MINUTE_OF_HOUR, time.minute);
		  this.start = this.end.plusMinutes(-duration);
	  }
  }
  
  public void updateDate(DateValue date, String op) {
	  if (op.equals("start")) {
		  if (date.day != -1) this.start = this.start.with(ChronoField.DAY_OF_MONTH, date.day);
		  if (date.month != -1) this.start = this.start.with(ChronoField.MONTH_OF_YEAR, date.month);
		  if (date.year != -1) this.start = this.start.with(ChronoField.YEAR, date.year);
	  }
	  else if (op.equals("end")) {
		  if (date.day != -1) this.end = this.end.with(ChronoField.DAY_OF_MONTH, date.day);
		  if (date.month != -1) this.end = this.end.with(ChronoField.MONTH_OF_YEAR, date.month);
		  if (date.year != -1) this.end = this.end.with(ChronoField.YEAR, date.year);
	  }
  }
  
  public void moveDate(DateValue date, String op) {
  	long duration = this.start.until(this.end, ChronoUnit.MINUTES);
	  if (op.equals("start")) {
		  if (date.day != -1) this.start = this.start.with(ChronoField.DAY_OF_MONTH, date.day);
		  if (date.month != -1) this.start = this.start.with(ChronoField.MONTH_OF_YEAR, date.month);
		  if (date.year != -1) this.start = this.start.with(ChronoField.YEAR, date.year);
		  this.end = this.start.plusMinutes(duration);
	  }
	  else if (op.equals("end")) {
		  if (date.day != -1) this.end = this.end.with(ChronoField.DAY_OF_MONTH, date.day);
		  if (date.month != -1) this.end = this.end.with(ChronoField.MONTH_OF_YEAR, date.month);
		  if (date.year != -1) this.end = this.end.with(ChronoField.YEAR, date.year);
		  this.start = this.end.plusMinutes(-duration);
	  }
  }
  
  public void updateDuration(NumberValue n) {
    double x = ((NumberValue) n).value;
		Integer d = new Integer((int) x);
		
		this.end = this.start;
		if (n.unit.equals("hours")) { 
			this.end = this.end.plusHours(d - start.until(end, ChronoUnit.HOURS));
			
			// handle fractions only for hours
			Double minutes = (x - Math.floor(x)) * 60.0;
			if (minutes > 4.5) {
				Integer min = minutes.intValue();
				min = min - (min % 5); // floor to 5
				this.end = this.end.plusMinutes(min);
			}
		}
		else if (n.unit.equals("minutes"))
			this.end = this.end.plusMinutes(d - start.until(end, ChronoUnit.MINUTES));
  }

  public void moveWeekday(int weekday, String op) {
	  if (weekday == -1) return;
	  
	  if (op.equals("start")) {
		  while (this.start.getDayOfWeek().getValue() != weekday) {  
			  this.start = this.start.plusDays(1);
			  this.end = this.end.plusDays(1);
		  }
	  }
	  else if (op.equals("end")) {
		  while (this.end.getDayOfWeek().getValue() != weekday) {
		  	this.start = this.start.plusDays(1);
			  this.end = this.end.plusDays(1);
		  }
	  }
  }
  
  //advance start or end to next occurrence of weekday (Mon = 1, Sun = 7)
  public void updateWeekday(int weekday, String op) {
	  if (weekday == -1) return; // could not resolve weekday to integer value
	  
	  if (op.equals("start")) {
		  while (this.start.getDayOfWeek().getValue() != weekday) {  
			  this.start = this.start.plusDays(1);
		  }
	  }
	  else if (op.equals("end")) {
		  while (this.end.getDayOfWeek().getValue() != weekday) {  
			  this.end = this.end.plusDays(1);
		  }
	  }
  }
  

  @SuppressWarnings("unchecked")
  public static Event fromJSON(String json) {
    List<Object> props = Json.readValueHard(json, List.class);
    return fromJSONObject(props);
  }
  @SuppressWarnings("unchecked")
  public static Event fromJSONObject(List<Object> props) {
    Event retcube = new Event();
    retcube.title = ((String)props.get(0));
    retcube.location = ((String)props.get(1));
    retcube.start = LocalDateTime.parse(((String)props.get(2)));
    retcube.end = LocalDateTime.parse(((String)props.get(3)));
//    retcube.repeats = ((List<Integer>)props.get(4));
//    retcube.guests = ((HashSet<Person>)props.get(5));

    retcube.names.addAll((List<String>)props.get(4));
    return retcube;
  }
  public Object toJSON() {
  	List<String> globalNames = names.stream().collect(Collectors.toList());
//    List<Object> event = Lists.newArrayList(title, location, start.toString(), end.toString(), repeats.toString(), guests.toString(), globalNames);
    List<Object> event = Lists.newArrayList(title, location, start.toString(), end.toString(), globalNames);
    return event;
  }

  @Override
  public Event clone() {
    return new Event(this.title, this.location, this.start, this.end, this.repeats, this.guests);
  }
  @Override
  public int hashCode() {
    final int prime = 53;
    int result = 1;
    for (int i = 0; i < this.title.length(); i++) result = prime * result + title.charAt(i);
    result = prime * result + this.start.hashCode();
    result = prime * result + this.end.hashCode();
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    
  	if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
        
    Event other = (Event) obj;
    if (title != other.title)
      return false;
	  if (location != other.location)
	      return false;
	  if (start != other.start)
	      return false;
	  if (end != other.end)
	      return false;
	  // TODO locations, repeats
	  
    return true;
  }
}

class Utils {
  // convert weekday to int (mon - 1, sun - 7), else -1
  public static int weekdayToInt(String weekday) {
  	weekday = weekday.toLowerCase();
	  switch (weekday) {
	  	case ("monday") : case ("mon") : case ("mn") : case ("m") :
	  		return 1;
	  	case ("tuesday") : case ("tue") : case ("tu") :
	  		return 2;
	  	case ("wednesday") : case ("wed") : case ("we") : case ("w") :
	  		return 3;
	  	case ("thursday") : case ("thur") : case ("thu") : case("th") :
	  		return 4;
	  	case ("friday") : case ("fri") : case ("fr") :
	  		return 5;
	  	case ("saturday") : case ("sat") : case ("sa") :
	  		return 6;
	  	case ("sunday") : case ("sun") : case ("su") :
	  		return 7;
	  }
	  return -1;
  }
}