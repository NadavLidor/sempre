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

  	// target
  	String targetEvents = target.toString().replace("\\", "");
  	targetEvents = targetEvents.substring(8, targetEvents.length() - 1);
  	EventsWorld targetWorld = EventsWorld.fromJSON(targetEvents);
  	
  	// prev
  	String predEvents = pred.toString().replace("\\", "");
  	predEvents = predEvents.substring(8, predEvents.length() - 1);
  	EventsWorld predWorld = EventsWorld.fromJSON(predEvents);
  	
  	
  	if (targetWorld.allitems.size() != predWorld.allitems.size()) {
  		LogInfo.log("NOT equals: different number of allitems");
  		return 0;
  	}
  	if (targetWorld.selected.size() != predWorld.selected.size()) {
  		LogInfo.log("NOT equals: different number of selected");
  		return 0;
  	}
  	
  	targetWorld.allitems.removeAll(predWorld.allitems);
  	
  	if (targetWorld.allitems.size() > 0) {
  		LogInfo.log("NOT equals: difference:");
  		for (Item i : targetWorld.allitems) {
  			Event e = (Event)i; 
//  			LogInfo.log(e.title + " at " + e.location);
//  			LogInfo.log(e.start);
//  			LogInfo.log(e.end);
//  			LogInfo.log("----");
  			
  		}
  		for (Item i : predWorld.allitems) {
  			Event e = (Event)i; 
//  			LogInfo.log(e.title + " at " + e.location);
//  			LogInfo.log(e.start);
//  			LogInfo.log(e.end);
//  			LogInfo.log("----");
  		}
//  		LogInfo.log("-------------------");
  		return 0;
  	}
  	
  	if (target.equals(pred)) LogInfo.log("FOUND EQUALS");
  	else LogInfo.log("NOT equals");
    return target.equals(pred) ? 1 : 0;
  }
}
