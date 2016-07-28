package edu.stanford.nlp.sempre.interactive.actions;

import static org.testng.AssertJUnit.assertEquals;

import java.util.*;
import java.util.function.Predicate;

import fig.basic.*;
import edu.stanford.nlp.sempre.*;

import org.testng.Assert;
import org.testng.annotations.Test;

//(date 2016 07 22)

/**
 * Test the ActionExecutor
 * @author Nadav Lidor
 */
public class ActionExecutorEventTest {
  ActionExecutor executor = new ActionExecutor();

  protected static void runFormula(ActionExecutor executor, String formula, ContextValue context, Predicate<FlatWorld> checker) {
  	executor.opts.FlatWorldType = "EventsWorld";
    LogInfo.begin_track("formula: %s", formula);
    Executor.Response response = executor.execute(Formulas.fromLispTree(LispTree.proto.parseFromString(formula)), context);
    
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph)context.graph;
    String wallString = ((StringValue)graph.triples.get(0).e1).value;
    String jsonStr = ((StringValue)response.value).value;
    LogInfo.logs("Start:\t%s", wallString);
    LogInfo.logs("Result:\t%s", jsonStr);
    LogInfo.end_track();
    
    if (checker != null) {
      if (!checker.test(FlatWorld.fromContext("EventsWorld", getContext(jsonStr)))) {
        Assert.fail(jsonStr);
      }
    }
  }

  private static ContextValue getContext(String events) {
    // a hack to pass in the world state without much change to the code
    String strigify2 = Json.writeValueAsStringHard(events); // some parsing issue inside lisptree parser
    return ContextValue.fromString(String.format("(context (graph NaiveKnowledgeGraph ((string \"%s\") (name b) (name c))))", strigify2));
  }
  
  public Predicate<FlatWorld> selectedSize(int n) {
    return x -> {LogInfo.logs("Got %d, expected %d", x.selected().size(), n); return x.selected().size()==n;};
  }
  
//  @Test public void testJoin() {
//    String defaultEvents = "["
//    		+ "[\"Meeting_Pam\",\"office\",\"2016-07-21T10:15:00\",\"2016-07-22T11:15:00\",[]],"
//    		+ "[\"Group_Meeting\",\"office\",\"2016-07-21T10:15:00\",\"2016-07-22T11:15:00\",[]],"
//    		+ "[\"Lunch\",\"cafe\",\"2016-07-23T12:00:00\",\"2016-07-23T13:30:00\",[]],"
//    		+ "[\"Lunch\",\"bar\",\"2016-07-24T12:00:00\",\"2016-07-24T13:30:00\",[]]"
//    		+ "]";
//    ContextValue context = getContext(defaultEvents);
//    LogInfo.begin_track("testJoin");
//
//    runFormula(executor, "(: select *)", context, selectedSize(4));
//    
//    LogInfo.end_track();
//
//  }
  
  @Test public void testBasicActions() {
    String defaultEvents = "["
    		+ "[\"Meeting_Pam\",\"office\",\"2016-07-22T10:15:00\",\"2016-07-22T11:15:00\",[]],"
    		+ "[\"Group_Meeting\",\"office\",\"2016-07-21T10:15:00\",\"2016-07-22T17:15:00\",[]],"
    		+ "[\"Lunch\",\"cafe\",\"2016-07-23T12:00:00\",\"2016-07-23T13:30:00\",[]],"
    		+ "[\"Lunch\",\"bar\",\"2016-07-27T15:00:00\",\"2016-07-27T16:00:00\",[]]"
    		+ "]";
    ContextValue context = getContext(defaultEvents);
    LogInfo.begin_track("testBasicActions");
//    
//    runFormula(executor, "(:s (: select *) (: remove) (: remove))", context, x -> x.allitems.size() == 0);
//    runFormula(executor, "(:s (: select *) (: update title (string new_title)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (title (string Lunch))) (: remove))", context, x -> x.allitems.size() == 2);
//    runFormula(executor, "(:s (: select (title (string Lunch))) (: update title (string replaced_TITLE)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (title (string Lunch))) (: update location (string replaced_LOCATION)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (title (string Lunch))) (: update start_weekday (string wed)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (title (string Lunch))) (: update end_weekday (string friday)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (title (string Lunch))) (: move start_weekday (string sat)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (title (string Lunch))) (: move end_weekday (string sun)))", context, x -> x.allitems.size() == 4);
//    
//    // new duration
//    runFormula(executor, "(:s (: select *) (: update duration (number 90 minutes)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select *) (: update duration (number 3 hours)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (duration (number 90 minutes))) (: update duration (number 90 minutes)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(: select (duration (number 60 minutes)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(: select (duration (number 60 minutes)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (duration (number 60 minutes))) (: remove))", context, x -> x.allitems.size() == 3);
//    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: remove))", context, x -> x.allitems.size() == 3);
//    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: update duration (number 5 hours)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: update duration (number 30 minutes)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: update duration (number 1.5 hours)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (duration (number 1.5 hours))) (: update duration (number 2.5 hours)))", context, x -> x.allitems.size() == 4);						
//    						
//    // date (update)
//    runFormula(executor, "(:s (: select *) (: update start_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select *) (: update end_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select *) (: update start_date (date 1990 08 03)) (: update end_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (start_date (date 2016 07 21))) (: remove))", context, x -> x.allitems.size() == 3);
//    runFormula(executor, "(:s (: select (end_date (date 2016 07 22))) (: remove))", context, x -> x.allitems.size() == 2);
//    
//    // date (move)
//    runFormula(executor, "(:s (: select *) (: move start_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select *) (: move end_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
//    
//    // time (update)
//    runFormula(executor, "(:s (: select *) (: update start_time (time 13 30)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select *) (: update start_time (time 13 30)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select (start_time (time 12 00))) (: remove))", context, x -> x.allitems.size() == 2);
//    runFormula(executor, "(:s (: select (end_time (time 14 00))) (: remove))", context, x -> x.allitems.size() == 3);
//    
//    // time (move)
//    runFormula(executor, "(:s (: select *) (: move start_time (time 13 30)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select *) (: move end_time (time 13 30)))", context, x -> x.allitems.size() == 4);
//    
//    // new event
//    runFormula(executor, "(: add)", context, x -> x.allitems.size() == 5);
    
    
    // relative
//    runFormula(executor, "(:s (: select *) (: update start_datetime (call now)))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: select *) (: move start_datetime (call now)))", context, x -> x.allitems.size() == 4);
    
    // move my 3pm meeting to 30 minutes from now
    runFormula(executor, "(:s (: select (and (start_date (date 2016 07 27)) (start_time (time 15 00)))) (: move start_datetime (call addtime (call now) (number 30 minutes))))", context, x -> x.allitems.size() == 4);
    
    // postpone my 3pm meeting by 30 minutes
//    runFormula(executor, "(:s (: select ...) (: update start_datetime (call addtime ((reverse start_datetime) this) (number 30 minutes))))", context, x -> x.allitems.size() == 4);
//    
//    runFormula(executor, "(:s (: select (and (start_date (call now)) (start_time (time 15 00)))) (: move start_datetime (call addtime ((reverse start_datetime) this) (number 30 minutes))))", context, x -> x.allitems.size() == 4);
//    																																																																						pick first?
    // (:s (select eventSet) (update start_time (valueSet))) --> no
    // (:s (select eventSet) (add start_time (value)))
    
    // postpone (all) my 3pm meeting (today) by half 30 minutes
    runFormula(executor, "(:s (: select (and (start_date (date 2016 07 27)) (start_time (time 15 00)))) (: add start_datetime (number 30 minutes)))", context, x -> x.allitems.size() == 4);
    
    
    // set meeting to 30 minutes after my 3pm meeting
    runFormula(executor, "(:s (: select *) (: update start_datetime (call addtime ((reverse end_datetime) (and (start_date (date 2016 07 27)) (start_time (time 15 00)))) (number 30 minutes))))", context, x -> x.allitems.size() == 4);
    
    // right now, reverse is returning a SET and i pick randomly from it. what should we do?
    // i also changed NOW to return the current time, wrapped in a set, to match reverse and make addtime work
    
    // bulk changes: postpone all meeting tomorrow
    // 
    
    
//    runFormula(executor, "(:s (: add) (: update start_datetime (call addtime (call now) (number 30 minutes))))", context, x -> x.allitems.size() == 4);
//    runFormula(executor, "(:s (: add) (: update start_datetime (call addtime (call current) (number 30 minutes))))", context, x -> x.allitems.size() == 4);
    
//    runFormula(executor, "(:s (: select *) (: add_time start_time ((call now) (number 30 minutes))))", context, x -> x.allitems.size() == 4);
    
    
    
    /***
    (: new_event) (: update start_time (call add_time (call now) (number 30 minutes))
    		(: new_event) (: update start_time (call add_time (call now) (number 1 day))
    	(some selected set)  (: update start_time (call add_time ((reverse start_time) this) (number 30 minutes))
    			(: move start_time (call add_time ((reverse start_time) this) (number 30 minutes))
    					
    					
    					next meeting (event e) 
    					first/last meeting of the day (eventset)
    ***/
    
    LogInfo.end_track();
  }
  
}