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
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.TimeValue;
import fig.basic.LogInfo;

// the world of stacks
public class EventsWorld extends FlatWorld {
  
//	private int counter = 1;
	
	public void select(Set<Item> set) {
		this.selected = set;
	} //TODO
	
  public static EventsWorld fromContext(ContextValue context) {
    if (context == null || context.graph == null) {
    	
    	String defaultEvents = "["
      		+ "[\"Meeting_Today_1\",\"office\",\"2016-08-08T10:15\",\"2016-08-08T11:15\",[false,false,false,false,false,false,false,false,false],[]],"
      		+ "[\"Meeting_Today_2\",\"cafe\",\"2016-08-08T13:00\",\"2016-08-08T14:30\",[false,false,false,false,false,false,false,false,false],[]],"
      		+ "[\"Meeting_Tomorrow\",\"bar\",\"2016-08-09T15:00\",\"2016-08-10T16:00\",[false,false,false,true,false,false,false,false,false],[]]" //repeats on Thu.
      		+ "]";
      return fromJSON(defaultEvents);
    }
    
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph)context.graph;
    String wallString = ((StringValue)graph.triples.get(0).e1).value;
    return fromJSON(wallString);
  }
  
  @SuppressWarnings("unchecked")
  public EventsWorld(Set<Item> eventset) {
    super();
    this.allitems = eventset;
    this.selected = eventset.stream().filter(e -> ((Event)e).names.contains("S")).collect(Collectors.toSet());
  }

  public String toJSON() {
    this.selected().forEach(e -> ((Event)e).names.add("S"));
    return Json.writeValueAsStringHard(this.allitems.stream().map(c -> ((Event)c).toJSON()).collect(Collectors.toList()));
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
//    LogInfo.log("HAS EVENTWORLD: " + values);
    if (rel.equals("repeat")) {
      return this.allitems.stream().filter(i -> !Collections.disjoint(values, (Collection<?>) i.get(rel)))
          .collect(Collectors.toSet());
    }
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
  	LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
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
  	LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
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
  	if (values == null || values.isEmpty()) return new HashSet<Item>();
    return this.allitems.stream().filter(i -> isAfter(values, i.get(rel)))
        .collect(Collectors.toSet());
  }
  
  public Set<Item> before(String rel, Set<Object> values) {
//  	LogInfo.log("BEFORE EVENTWORLD: " + values + " " + rel);
  	if (values == null || values.isEmpty()) return new HashSet<Item>();
    return this.allitems.stream().filter(i -> isBefore(values, i.get(rel)))
        .collect(Collectors.toSet());
  }
  
  public boolean isAfter(Set<Object> values, Object v) {
  	if (v instanceof DateValue) {
  		for (Object o : values) {
    		if (o != null && o instanceof DateValue && ((DateValue)v).isAfter((DateValue)o)) 
    			return true;
    	}
  	}
  	if (v instanceof TimeValue) {
  		for (Object o : values) {
    		if (o != null && o instanceof TimeValue && ((TimeValue)v).isAfter((TimeValue)o)) 
    			return true;
    	}
  	}
  	if (v instanceof LocalDateTime) {
  		for (Object o : values) {
    		if (o != null && o instanceof LocalDateTime && ((LocalDateTime)v).isAfter((LocalDateTime)o))  // TODO the null check??
    			return true;
    	}
  	}
  	if (v instanceof DateTimeValue) {
  		for (Object o : values) {
    		if (o != null && o instanceof DateTimeValue && ((DateTimeValue)v).isAfter((DateTimeValue)o)) 
    			return true;
    	}
  	}
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
  
  
  public boolean isBefore(Set<Object> values, Object v) {
  	if (v instanceof DateValue) {
  		for (Object o : values) {
    		if (o != null && o instanceof DateValue && ((DateValue)v).isBefore((DateValue)o)) 
    			return true;
    	}
  	}
  	if (v instanceof TimeValue) {
  		for (Object o : values) {
    		if (o != null && o instanceof TimeValue && ((TimeValue)v).isBefore((TimeValue)o)) 
    			return true;
    	}
  	}
  	if (v instanceof LocalDateTime) {
  		for (Object o : values) {
    		if (o != null && o instanceof LocalDateTime && ((LocalDateTime)v).isBefore((LocalDateTime)o)) //TODO the null check?? 
    			return true;
    	}
  	}
  	if (v instanceof DateTimeValue) {
  		for (Object o : values) {
    		if (o != null && o instanceof DateTimeValue && ((DateTimeValue)v).isBefore((DateTimeValue)o)) 
    			return true;
    	}
  	}
  	return false;
  }
  
  
  public LocalDateTime now(){
//  	return LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
  	return LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
  	
  }
  
  public LocalDateTime todaystart(){ // 12 am
//  	return LocalDateTime.now().withHour(0).truncatedTo(ChronoUnit.HOURS);
  	return LocalDateTime.now(ZoneId.of("UTC+00:00")).withHour(0).truncatedTo(ChronoUnit.HOURS);
  }
  
//  public DateValue today(){
//  	LocalDateTime n = LocalDateTime.now();
//    return new DateValue(n.getYear(), n.getMonthValue(), n.getDayOfMonth());
//  }
  
  public LocalDateTime weekstart(){
  	LocalDateTime n = LocalDateTime.now(ZoneId.of("UTC+00:00"));
  	n = n.plusDays(1 -n.getDayOfWeek().getValue()); // reset to previous Monday
  	n = n.withHour(0).truncatedTo(ChronoUnit.HOURS); // reset to midnight
    return n;
  }
  
  public LocalDateTime monthstart(){
  	LocalDateTime n = LocalDateTime.now(ZoneId.of("UTC+00:00"));
  	n = n.plusDays(1 -n.getDayOfMonth()); // reset to the 1st of this month
  	n = n.withHour(0).truncatedTo(ChronoUnit.HOURS); // reset to midnight
    return n;
  }
  
//  public toDateTime () {
//  	
//  }
  
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
  
  public NumberValue numberunit (NumberValue n, String s) {
  	return new NumberValue(n.value, s);
  }
  

  public void reset(String name) {
    this.allitems.clear();
    this.selected.clear();
    
    
    Event n = new Event();
    n.title = "Lunch";
    n.start = n.start.withHour(12);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(13);
    n.start = n.start.plusDays(-7);
    n.end = n.end.plusDays(-7);
    for (int i = 0 ; i < 14; i++) {
      n.start = n.start.plusDays(1);
      n.end = n.end.plusDays(1);
    	this.allitems.add(n.clone());
    }
    
    n = new Event();
    n.title = "Dinner";
    n.start = n.start.withHour(19);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(20);
    n.start = n.start.plusDays(-7);
    n.end = n.end.plusDays(-7);
    for (int i = 0 ; i < 14; i++) {
      n.start = n.start.plusDays(1);
      n.end = n.end.plusDays(1);
    	this.allitems.add(n.clone());
    }
    
    n = new Event();
    n.title = "Meeting1";
    n.start = n.start.withHour(9);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(10);
    n.end = n.end.withMinute(45);
    n.start = n.start.plusDays(-7);
    n.end = n.end.plusDays(-7);
    for (int i = 0 ; i < 7; i++) {
      n.start = n.start.plusDays(3);
      n.end = n.end.plusDays(3);
    	this.allitems.add(n.clone());
    }

    n = new Event();
    n.title = "Meeting2";
    n.start = n.start.withHour(14);
    n.start = n.start.withMinute(0);
    n.end = n.start.withHour(16);
    n.start = n.start.plusDays(-7);
    n.end = n.end.plusDays(-7);
    for (int i = 0 ; i < 7; i++) {
      n.start = n.start.plusDays(2);
      n.end = n.end.plusDays(2);
    	this.allitems.add(n.clone());
    }
    
    
    
//    this.allitems.add(new Event("meeting2", "location2", n.start.plusMinutes(120), n.end.plusMinutes(180), n.repeats, n.guests));
//    this.allitems.add(new Event("meeting3", "location3", n.start.plusHours(24), n.end.plusHours(48), n.repeats, n.guests));
    
  }
  
  public Set<Item> all() {
  	return this.allitems;
  }
  
  public void add() {
//    List<Boolean> repeats = new ArrayList<Boolean>(Collections.nCopies(9, false));
//    HashSet<Person> guests = new HashSet<Person>();
//    LocalDateTime start = LocalDateTime.now(ZoneId.of("UTC+00:00")).truncatedTo(ChronoUnit.MINUTES);
//    Event e = new Event("new_meeting", "new_location", start, start.plusMinutes(120), repeats, guests);
  	Event e = new Event();
  	e.title = e.title;
    this.allitems.add(e);
    this.selected.clear();
    this.selected.add(e); 
  }
  
//  public void add() {
//  	Item event  = new Event();
////  	((Event)event).names.add("S");
//  	this.selected.clear();
//  	this.allitems.add(event);
//  	this.selected.add(event);
//  }
  
}
