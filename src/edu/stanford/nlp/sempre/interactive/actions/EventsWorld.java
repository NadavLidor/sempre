package edu.stanford.nlp.sempre.interactive.actions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.DateTimeValue;
import edu.stanford.nlp.sempre.DateValue;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.NaiveKnowledgeGraph;
import edu.stanford.nlp.sempre.NumberValue;
import edu.stanford.nlp.sempre.SUDateValue;
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.TimeValue;
import fig.basic.LogInfo;

// the world of stacks
public class EventsWorld extends FlatWorld {
  
	private LocalDateTime datetime;
	
	public void select(Set<Item> set) {
//		this.selected().forEach(ev -> ((Event)ev).names.clear()); TODO?
		this.selected = set;
	} //TODO
	
  public LocalDateTime calendarTime() {
  	return this.datetime;
  	//LocalDateTime.parse("2015-11-11T12:59:00");
  }
	
  public static EventsWorld fromContext(ContextValue context) {
    if (context == null || context.graph == null) {
    	
//    	String defaultEvents = "["
//      		+ "[\"Meeting_Today_1\",\"office\",\"2016-08-08T10:15\",\"2016-08-08T11:15\",[false,false,false,false,false,false,false,false,false],[]],"
//      		+ "[\"Meeting_Today_2\",\"cafe\",\"2016-08-08T13:00\",\"2016-08-08T14:30\",[false,false,false,false,false,false,false,false,false],[]],"
//      		+ "[\"Meeting_Tomorrow\",\"bar\",\"2016-08-09T15:00\",\"2016-08-10T16:00\",[false,false,false,true,false,false,false,false,false],[]]" //repeats on Thu.
//      		+ "]";
    	
    	String defaultEvents = "["
      		+ "[\"Meeting_Today_1\",\"office\",\"2016-08-08T10:15\",\"2016-08-08T11:15\",[]],"
      		+ "[\"Meeting_Today_2\",\"cafe\",\"2016-08-08T13:00\",\"2016-08-08T14:30\",[]],"
      		+ "[\"Meeting_Tomorrow\",\"bar\",\"2016-08-09T15:00\",\"2016-08-10T16:00\",[]]" //repeats on Thu.
      		+ "]";
      return fromJSON(defaultEvents, new DateTimeValue(LocalDateTime.now()));
    }
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph)context.graph;
    String wallString = ((StringValue)graph.triples.get(0).e1).value;
    return fromJSON(wallString, context.datetime);
  }
  
  @SuppressWarnings("unchecked")
  public EventsWorld(Set<Item> eventset, DateTimeValue currentTime) {
    super();
    this.allitems = eventset;
    this.selected = eventset.stream().filter(e -> ((Event)e).names.contains("S")).collect(Collectors.toSet());
    this.datetime = currentTime.datetime;
  }
  
  public EventsWorld(Set<Item> eventset) {
    super();
    this.allitems = eventset;
    this.selected = eventset.stream().filter(e -> ((Event)e).names.contains("S")).collect(Collectors.toSet());
    this.datetime = LocalDateTime.now();
  }

  public String toJSON() {
//  	LogInfo.log("this.selected().toString()"); TODO there's a bug with the 'S' and 'N'
//  	LogInfo.log(this.selected().toString());
    this.selected().forEach(e -> ((Event)e).names.add("S"));
    return Json.writeValueAsStringHard(this.allitems.stream().map(c -> ((Event)c).toJSON()).collect(Collectors.toList()));
  }

  public static EventsWorld fromJSON(String wallString, DateTimeValue currentTime) {
  	@SuppressWarnings("unchecked")
    List<List<Object>> eventstr = Json.readValueHard(wallString, List.class);
    Set<Item> events = eventstr.stream().map(e -> {return Event.fromJSONObject(e);})
        .collect(Collectors.toSet());
    return new EventsWorld(events, currentTime);
  }
  
  public static EventsWorld fromJSON(String wallString) {
  	@SuppressWarnings("unchecked")
    List<List<Object>> eventstr = Json.readValueHard(wallString, List.class);
    Set<Item> events = eventstr.stream().map(e -> {return Event.fromJSONObject(e);})
        .collect(Collectors.toSet());
    return new EventsWorld(events);
  }

  @Override
  public Set<Item> has(String rel, Set<Object> values) {
    LogInfo.log("HAS EVENTWORLD: " + values);
//    Set<Object> temp = new HashSet<Object>();
    if (rel.equals("repeat")) {
      return this.allitems.stream().filter(i -> !Collections.disjoint(values, (Collection<?>) i.get(rel)))
          .collect(Collectors.toSet());
    }
    
//    for (Object o : values) {
//    	if (o instanceof SUDateValue) {
//    		temp.add(DateValue.parseSUDateValue(((SUDateValue)o).date, EventsWorld.calendarTime()));
//    	} else {
//    		break;
//    	}
//    }
//    if (temp.size() > 0) values = temp;
    
    return this.allitems.stream().filter(i -> values.contains(i.get(rel)))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Object> get(String rel, Set<Item> subset) {
    return subset.stream().map(i -> i.get(rel))
        .collect(Collectors.toSet());
  }

  @Override
  public void update(String rel, Object value, Set<Item> selected) {
//  	this.allitems.removeAll(selected);
    selected.forEach(i -> i.update(rel, value));
//    this.allitems.addAll(selected);
  }
  
  public void add(String rel, Object value) {
  	selected.forEach(i -> i.add(rel, value));
  }
  
  // calendar world specific actions TODO
  public void move(String rel, Object value) {
  	this.selected.forEach(i -> i.move(rel, value));
  }
    
  
  public static Set<Item> argmax(Set<Item> items, Function<Event, Integer> f) {
    int maxvalue = Integer.MIN_VALUE;
    for (Item i : items) {
      int cvalue = f.apply((Event)i);
      if (cvalue > maxvalue) maxvalue = cvalue;
    }
    final int maxValue = maxvalue;
    return items.stream().filter(c -> f.apply((Event)c) >= maxValue).collect(Collectors.toSet());
  }
  
  // custom functions
  public Set<Item> pick_first(String rel, Set<Item> s) {
  	Set<Item> res = new HashSet<Item>();
  	if (s.isEmpty()) return res;
  	if (rel.equals("start_datetime") || rel.equals("end_datetime")) {
  		LocalDateTime last = LocalDateTime.MAX;
    	Item t = null;
    	for (Item i : s) {
    		LocalDateTime temp = ((LocalDateTime)i.get(rel));
    		if (temp.isBefore(last)) {
    			last = temp;
    			t = i;
    		}
    	}
    	if (t != null) res.add(t);
    	return res;
//    	return t;
  	}
  	throw new RuntimeException("EventsWorlds: cannot pick the first from ItemSet " + s.toString() + " based on attribute " + rel);
  }
  
  // custom functions
  public Set<Item> pick_next(String rel, Set<Item> s) {
  	Set<Item> res = new HashSet<Item>();
  	if (s.isEmpty()) return res;
  	LocalDateTime now = calendarTime();
  	if (rel.equals("start_datetime") || rel.equals("end_datetime")) {
  		LocalDateTime last = LocalDateTime.MAX;
    	Item t = null;
    	for (Item i : s) {
    		LocalDateTime temp = ((LocalDateTime)i.get(rel));
    		if (temp.isBefore(last) && temp.isAfter(now)) {
    			last = temp;
    			t = i;
    		}
    	}
    	if (t != null) res.add(t);
    	return res;
//    	return t;
  	}
  	throw new RuntimeException("EventsWorlds: cannot pick the first from ItemSet " + s.toString() + " based on attribute " + rel);
  }
  
  
  
  // custom functions
  public Set<Item> pick_last(String rel, Set<Item> s) {
  	Set<Item> res = new HashSet<Item>();
  	if (s.isEmpty()) return res;
  	if (rel.equals("start_datetime") || rel.equals("end_datetime")) {
  		LocalDateTime last = LocalDateTime.MIN;
    	Item t = null;
    	for (Item i : s) {
    		LocalDateTime temp = ((LocalDateTime)i.get(rel));
    		if (temp.isAfter(last)) {
    			last = temp;
    			t = i;
    		}
    	}
    	if (t != null) res.add(t);
    	return res;
  	}
  	throw new RuntimeException("EventsWorlds: cannot pick the last from ItemSet " + s.toString() + " based on attribute " + rel);
  }
  
  public Set<Item> pick_prev(String rel, Set<Item> s) {
  	Set<Item> res = new HashSet<Item>();
  	if (s.isEmpty()) return res;
  	LocalDateTime now = calendarTime();
  	if (rel.equals("start_datetime") || rel.equals("end_datetime")) {
  		LocalDateTime last = LocalDateTime.MIN;
    	Item t = null;
    	for (Item i : s) {
    		LocalDateTime temp = ((LocalDateTime)i.get(rel));
    		if (temp.isAfter(last) && temp.isBefore(now)) {
    			last = temp;
    			t = i;
    		}
    	}
    	if (t != null) res.add(t);
    	return res;
  	}
  	throw new RuntimeException("EventsWorlds: cannot pick the last from ItemSet " + s.toString() + " based on attribute " + rel);
  }
  
  public Set<Item> after(String rel, Set<Object> values) {
//  	LogInfo.log("AFTER EVENTWORLD: " + values);
  	if (values == null || values.isEmpty()) 
  		return new HashSet<Item>();
  	
  	Object value = null;
  	for (Object o : values) {
  		value = o;
  	}
  	if (value instanceof SUDateValue)
  		value = DateValue.parseSUDateValue(((SUDateValue)value).date, calendarTime());
  	
  	final Object f = value;
    return this.allitems.stream().filter(i -> isAfter(f, i.get(rel)))
        .collect(Collectors.toSet());
  }
  
  public Set<Item> before(String rel, Set<Object> values) {
//  	LogInfo.log("BEFORE EVENTWORLD: " + values + " " + rel);
  	
  	
  	if (values == null || values.isEmpty()) 
  		return new HashSet<Item>();
  	
  	Object value = null;
  	for (Object o : values) {
  		value = o;
  	}
  	if (value instanceof SUDateValue)
  		value = DateValue.parseSUDateValue(((SUDateValue)value).date, calendarTime());
  	
  	final Object f = value;
    return this.allitems.stream().filter(i -> isBefore(f, i.get(rel)))
        .collect(Collectors.toSet());
  }
  
  public boolean isAfter(Object o, Object v) {
  	if (v instanceof SUDateValue) {
  			DateValue a = DateValue.parseSUDateValue(((SUDateValue)v).date, calendarTime());
    		return (a.isAfter((DateValue)o));
    }
  	if (v instanceof DateValue)
    	return ((DateValue)v).isAfter((DateValue)o);
  	if (v instanceof TimeValue)
    	return ((TimeValue)v).isAfter((TimeValue)o);
  	if (v instanceof LocalDateTime)
    	return ((LocalDateTime)v).isAfter((LocalDateTime)o);
  	if (v instanceof DateTimeValue)
    	return ((DateTimeValue)v).isAfter((DateTimeValue)o);
  	
  	LogInfo.log("EventsWorld.isAfter: object is not of right type");
  	return false;
  }
  
  public Object pick(Set<Object> values) {
  	HashSet<Object> res = new HashSet<Object>();
  	for (Object s : values) {
  		if (s != null) res.add(s);
  		break;
  	}
  	return res;
  }
  
  
  public boolean isBefore(Object o, Object v) {
  	if (v instanceof SUDateValue) {
			DateValue a = DateValue.parseSUDateValue(((SUDateValue)v).date, calendarTime());
  		return (a.isBefore((DateValue)o));
  	}
  	if (v instanceof DateValue)
  		return ((DateValue)v).isBefore((DateValue)o); 
  	if (v instanceof TimeValue)
  		return ((TimeValue)v).isBefore((TimeValue)o);
  	if (v instanceof DateTimeValue)
    	return ((DateTimeValue)v).isBefore((DateTimeValue)o);
  	if (v instanceof LocalDateTime)
  		return ((LocalDateTime)v).isBefore((LocalDateTime)o);  
  	
  	LogInfo.log("EventsWorld.isBefore: object is not of right type");
  	return false;
  }
  
  
  public LocalDateTime now() {
  	return calendarTime();
  }
  
  public LocalDateTime todaystart(){ // 12 am
  	return calendarTime().withHour(0).truncatedTo(ChronoUnit.HOURS);
  }
  
  public LocalDateTime weekstart(){
  	LocalDateTime n = calendarTime().withHour(0).truncatedTo(ChronoUnit.HOURS);
  	n = n.plusDays(1 -n.getDayOfWeek().getValue()); // reset to previous Monday
    return n;
  }
  
  public LocalDateTime monthstart(){
  	LocalDateTime n = calendarTime().withHour(0).truncatedTo(ChronoUnit.HOURS);
  	n = n.plusDays(1 -n.getDayOfMonth()); // reset to the 1st of this month
    return n;
  }
  
  //TODO //TODO
  public LocalDateTime addtime_2(LocalDateTime t, NumberValue n, Set<Item> selected) {
  	
  	if (n.unit.equals("minutes")) t = t.plusMinutes((int)n.value);
  	if (n.unit.equals("hours")) t = t.plusHours((int)n.value);
  	if (n.unit.equals("days")) t = t.plusDays((int)n.value);
  	if (n.unit.equals("months")) t = t.plusMonths((int)n.value);
  	if (n.unit.equals("years")) t = t.plusYears((int)n.value);
  	return t;
  }
  
  // todo, need to handle singleton sets better
  public LocalDateTime addtime(Set<LocalDateTime> timeset, Set<NumberValue> numbers, Set<Item> selected) {
  	
  	if (timeset == null || timeset.isEmpty() || numbers == null || numbers.isEmpty()) return null;
  	
  	LocalDateTime random = null;
  	for (LocalDateTime t : timeset) {
  		random = t;
  		break;
  	}
  	NumberValue random_n = null;
  	for (NumberValue n : numbers) {
  		random_n = n;
  		break;
  	}
  	return addtime_2(random, random_n, selected);
  }
  
  public LocalDateTime addtimereverse(Set<LocalDateTime> timeset, Set<NumberValue> numbers, Set<Item> selected) {
  	
  	if (timeset == null || timeset.isEmpty() || numbers == null || numbers.isEmpty()) return null;
  	
  	LocalDateTime random = null;
  	for (LocalDateTime t : timeset) {
  		random = t;
  		break;
  	}
  	NumberValue random_n = null;
  	for (NumberValue n : numbers) {
  		random_n = new NumberValue(-n.value, n.unit);
  		break;
  	}
  	return addtime_2(random, random_n, selected);
  }
  
  public NumberValue numberunit (NumberValue n, String s) {
  	return new NumberValue(n.value, s);
  }
  
  public void clear() {
  	this.allitems.clear();
    this.selected.clear();
  }
  
  
  public void reset(String name) {
    this.allitems.clear();
    this.selected.clear();
    Event n = new Event(this.datetime);
    
    /*
    n.title = "lunch with jack";
    n.location = "the usual";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.start = n.start.plusDays(-7); // wed
    n.end = n.end.plusDays(-7);
    this.allitems.add(n.clone());
    
    n.title = "meeting with sam";
    n.location = "";
    n.start = n.start.withHour(14);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(16);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "dinner with friends";
    n.location = "main street";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(20);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //thu
    n.end = n.end.plusDays(1);

    n.title = "morning sync";
    n.location = "office";
    n.start = n.start.withHour(10);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(11);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "lunch with sida";
    n.location = "nexus";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "work on project";
    n.location = "";
    n.start = n.start.withHour(14);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(16);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "gym";
    n.location = "main street";
    n.start = n.start.withHour(19);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(20);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //fri
    n.end = n.end.plusDays(1);
    
    n.title = "meeting with chris";
    n.location = "chris office";
    n.start = n.start.withHour(11);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(12);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "group lunch";
    n.location = "room 214";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "dinner with family";
    n.location = "italian cafe";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(20);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //sat
    n.end = n.end.plusDays(1);
    
    n.title = "brunch";
    n.location = "our place";
    n.start = n.start.withHour(9);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(12);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "park with kids";
    n.location = "";
    n.start = n.start.withHour(13);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(15);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //sun
    n.end = n.end.plusDays(1);
    
    n.title = "hike";
    n.location = "tbd";
    n.start = n.start.withHour(11);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(15);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "basketball";
    n.location = "";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(19);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    */
    	
    n.moveWeekday(-1, "start"); // go back to last monday
    
    n.start = n.start.plusDays(1); //mon
    n.end = n.end.plusDays(1);
    
    n.title = "project sync";
    n.location = "";
    n.start = n.start.withHour(9);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(11);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "team lunch";
    n.location = "room 300";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "dinner with dan";
    n.location = "tbd";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(19);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //tue
    n.end = n.end.plusDays(1);
    
    n.title = "meeting with will";
    n.location = "office";
    n.start = n.start.withHour(10);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(11);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "gym with nihil";
    n.location = "";
    n.start = n.start.withHour(19);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(20);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //wed
    n.end = n.end.plusDays(1);
    
    n.title = "take matthew to dentist";
    n.location = "";
    n.start = n.start.withHour(9);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(11);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "family dinner";
    n.location = "home";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(19);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //thu
    n.end = n.end.plusDays(1);
    
    n.title = "project status";
    n.location = "conference room";
    n.start = n.start.withHour(9);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(11);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "lunch with new hire";
    n.location = "";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "ideas dinner";
    n.location = "tressider";
    n.start = n.start.withHour(17);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(19);
    n.end = n.end.withMinute(00);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //fri
    n.end = n.end.plusDays(1);
    
    n.title = "team lunch";
    n.location = "";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "work with katie";
    n.location = "office";
    n.start = n.start.withHour(15);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(16);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "anniversary";
    n.location = "tbd";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(20);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //sat
    n.end = n.end.plusDays(1);
    
    n.title = "burnch with parents";
    n.location = "parents home";
    n.start = n.start.withHour(10);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(12);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "meeting bobby!";
    n.location = "";
    n.start = n.start.withHour(16);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(17);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //sun
    n.end = n.end.plusDays(1);
    
    n.title = "pool with kids";
    n.location = "";
    n.start = n.start.withHour(11);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(14);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.title = "wash car";
    n.location = "";
    n.start = n.start.withHour(15);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(15);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "soccer";
    n.location = "";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(20);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());

    /*
    
    n.start = n.start.plusDays(1); //next mon
    n.end = n.end.plusDays(1);
    
    n.title = "meeting chris";
    n.location = "chris office";
    n.start = n.start.withHour(11);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(12);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.title = "new team lunch";
    n.location = "";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //next tue
    n.end = n.end.plusDays(1);
    
    n.title = "dinner with charlie";
    n.location = "italian cafe";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(19);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //next wed
    n.end = n.end.plusDays(1);
    
    n.title = "meeting sam";
    n.location = "office";
    n.start = n.start.withHour(11);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(45);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //next thu
    n.end = n.end.plusDays(1);
    
    n.title = "dinner with jan";
    n.location = "home";
    n.start = n.start.withHour(18);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(19);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    
    n.start = n.start.plusDays(1); //next fri
    n.end = n.end.plusDays(1);
    
    n.title = "lunch with anthony";
    n.location = "bytes maybe";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.end = n.end.withMinute(0);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //next sat
    n.end = n.end.plusDays(1);
    
    n.title = "hiking";
    n.location = "park";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(30);
    n.end = n.start.withHour(16);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    n.start = n.start.plusDays(1); //next sun
    n.end = n.end.plusDays(1);
    
    n.title = "kids";
    n.location = "home";
    n.start = n.start.withHour(11);
    n.start = n.start.withMinute(00);
    n.end = n.start.withHour(15);
    n.end = n.end.withMinute(30);
    this.allitems.add(n.clone());
    
    */
    
  }
  
  public Set<Item> all() {
  	return this.allitems;
  }
  
  public void add() {
  	
  	Event e = new Event(this.datetime);
  	if (this.selected().contains(e)) return;
  	
  	// otherwise clear all selected
  	this.selected().forEach(ev -> ((Event)ev).names.clear());
  	this.selected.clear();
  	e.names.add("N");
    this.allitems.add(e);
    this.selected.add(e); 
  }
  
}
