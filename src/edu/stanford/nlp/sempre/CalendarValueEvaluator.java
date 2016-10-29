package edu.stanford.nlp.sempre;

import java.util.stream.Collectors;

import edu.stanford.nlp.sempre.interactive.actions.Event;
import edu.stanford.nlp.sempre.interactive.actions.EventsWorld;
import edu.stanford.nlp.sempre.interactive.actions.Item;
import fig.basic.LispTree;
import fig.basic.LogInfo;

// This is the simplest evaluator, but exact match can sometimes be too harsh.
public class CalendarValueEvaluator implements ValueEvaluator {
  public double getCompatibility(Value target, Value pred) {

  	LogInfo.log("getCompatibility: " + target);
  	
  	// target
  	String targetEvents = ((StringValue)target).value;
  	EventsWorld targetWorld = EventsWorld.fromJSON(targetEvents);
  	
  	// prev  	
  	String predEvents = ((StringValue)pred).value;
  	EventsWorld predWorld = EventsWorld.fromJSON(predEvents);
  	
  	if (targetWorld.allitems.size() != predWorld.allitems.size()) {
//  		LogInfo.log("NOT equals: different number of allitems");
  		return 0;
  	}
  	
  	double res = 1.0;
  	
  	if (targetWorld.selected.size() != predWorld.selected.size()) {
//  		LogInfo.log("Different number of selected");
  		res = 0.000001; // if selected number is off, max value returned is this
  	}
  	
  	// diff
  	targetWorld.allitems.removeAll(predWorld.allitems);
  	
  	if (targetWorld.allitems.size() > 0) {
//  		LogInfo.log("NOT equals: difference:");
  		return 0;
  		
//		for (Item i : targetWorld.allitems) {
//		Event e = (Event)i; 
//		LogInfo.log(e.title + " at " + e.location);
//		LogInfo.log(e.start);
//		LogInfo.log(e.end);
//		LogInfo.log("----");
//		
//	}
//	for (Item i : predWorld.allitems) {
//		Event e = (Event)i; 
//		LogInfo.log(e.title + " at " + e.location);
//		LogInfo.log(e.start);
//		LogInfo.log(e.end);
//		LogInfo.log("----");
//	}
//	LogInfo.log("-------------------");
  	}
  	
  	LogInfo.log("FOUND EQUALS");
  	return res;
  	
//  	else LogInfo.log("NOT equals");
//    return target.equals(pred) ? 1 : 0;
  }
}
