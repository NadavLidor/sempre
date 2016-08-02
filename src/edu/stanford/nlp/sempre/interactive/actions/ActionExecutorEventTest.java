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
  
  
  @Test public void testRelative() {
  	String defaultEvents = "["
    		+ "[\"Two_day_meeting\",\"office\",\"2016-08-02T10:15:00\",\"2016-08-03T11:15:00\",[false,false,false,false,false,false,false,false,false],[]],"
    		+ "[\"Lunch\",\"cafe\",\"2016-08-01T13:00:00\",\"2016-08-01T14:30:00\",[false,false,false,false,false,false,false,false,false],[]],"
    		+ "[\"Appointment\",\"bar\",\"2016-08-01T15:00:00\",\"2016-08-01T16:00:00\",[false,false,false,true,false,false,false,false,false],[]]" //repeats on Thu.
    		+ "]";
    ContextValue context = getContext(defaultEvents);
    LogInfo.begin_track("testRelativeActions");
    
    // sets for time ranges
    String today = "(and (call after start_datetime (call today_start)) (call before start_datetime (call addtime (call today_start) (number 24 hours))))";
    String tomorrow = "(and (call after start_datetime (call addtime (call today_start) (number 24 hours))) (call before start_datetime (call addtime (call today_start) (number 48 hours))))";
    String yesterday = "(and (call after start_datetime (call addtime (call today_start) (number -24 hours))) (call before start_datetime (call today_start)))";
    String next_week = "(and (call after start_datetime (call addtime (call week_start) (number 7 days))) (call before start_datetime (call addtime (call week_start) (number 14 days))))";
    
    // times of day (general, not day specific)
    
    
    String select_meeting = "(: select (and " + today + " (start_time (time 15 00))))";
    
    // move / update my 3pm to now
    runFormula(executor, "(:s " + select_meeting + " (: move start_datetime (call now)))", context, x -> x.allitems.size() == 3);
    runFormula(executor, "(:s " + select_meeting + " (: update start_datetime (call now)))", context, x -> x.allitems.size() == 3);
    
    // move my 3pm meeting to 30 minutes from now
    runFormula(executor, "(:s " + select_meeting + " (: move start_datetime (call addtime (call now) (number 30 minutes))))", context, x -> x.allitems.size() == 3);
    
    
    // postpone my meetings today by half 30 minutes
    runFormula(executor, "(:foreach " + today + " (: move start_datetime (call addtime ((reverse start_datetime) this) (number 30 minutes))))", context, x -> x.allitems.size() == 3);
    
    // change my 3pm meeting to be 30 minutes after my 10:15am meeting:
    runFormula(executor, "(:s " + select_meeting + " (: move start_datetime (call addtime ((reverse end_datetime) (and (start_date (date 2016 07 29)) (start_time (time 10 15)))) (number 30 minutes))))", context, x -> x.allitems.size() == 3);
    
    // cancel all my meeting in the next two hours WARNING: time dependent 
//    select_meeting = "(: select (and (call after start_datetime (call now)) (call before start_datetime (call addtime (call now) (number 2 hours)))))"; 
//    runFormula(executor, "(:s " + select_meeting + " (: remove))", context, x -> x.allitems.size() == 2);
    
    // cancel all my meetings today
    select_meeting = "(: select (and (call after start_datetime (call today_start)) (call before start_datetime (call addtime (call today_start) (number 24 hours)))))"; 
    runFormula(executor, "(:s " + select_meeting + " (: remove))", context, x -> x.allitems.size() == 1);
    
    // cancel all my meeting tomorrow morning
    select_meeting = "(: select (and (call after start_datetime (call addtime (call today_start) (number 30 hours))) (call before start_datetime (call addtime (call today_start) (number 36 hours)))))"; 
    runFormula(executor, "(:s " + select_meeting + " (: remove))", context, x -> x.allitems.size() == 2);
    
    // cancel my next meeting (next is 'first' across all)
    select_meeting = "(:s (: select ()) (: select (title (string Lunch))))";
    
    // move my meetings on Saturday morning to Sunday
    // version 1
    String sat_morning = "(and (call after start_datetime (call addtime (call weekstart) (number 126 hours))) (call before start_datetime (call addtime (call weekstart) (number 132 hours))))";
    runFormula(executor, "(:s (: select " + sat_morning + ") (: move start_weekday (number 7))))", context, x -> x.allitems.size() == 3);
    
    // version 2
    String sat = "(and (call after start_datetime (call addtime (call weekstart) (number 120 hours))) (call before start_datetime (call addtime (call weekstart) (number 144 hours))))";
    String morning = "(and (call after start_time (time 08 00)) (call before start_time (time 11 00)))";
    sat_morning = "(and " + sat + " " + morning + ")";
    runFormula(executor, "(:s (: select " + sat_morning + ") (: move start_weekday (number 7))))", context, x -> x.allitems.size() == 3);
    
    // move my meetings on the morning of the 30th to Sunday
    // with range
    String the_date = "(and (call after start_date (date 2016 07 29)) (call before start_date (date 2016 07 31)))"; //TODO date
    sat_morning = "(and " + the_date + " " + morning + ")";
    runFormula(executor, "(:s (: select " + sat_morning + ") (: move start_weekday (number 7))))", context, x -> x.allitems.size() == 3);
    // exact
    the_date = "(start_date (date 2016 07 30))"; //TODO date
    sat_morning = "(and " + the_date + " " + morning + ")";
    runFormula(executor, "(:s (: select " + sat_morning + ") (: move start_weekday (number 7))))", context, x -> x.allitems.size() == 3);
    
    // cancel my first meeting of the day
    select_meeting = "(: select (call pick_first start_datetime " + today + " ))";
    runFormula(executor, "(:s " + select_meeting + " (: remove))", context, x -> x.allitems.size() == 2);

    // cancel my last meeting of the day
    select_meeting = "(: select (call pick_last start_datetime " + today + " ))";
    runFormula(executor, "(:s " + select_meeting + " (: remove))", context, x -> x.allitems.size() == 2);
    
    // cancel my first meeting tomorrow
    select_meeting = "(: select (call pick_first start_datetime " + tomorrow + " ))";
    runFormula(executor, "(:s " + select_meeting + " (: remove))", context, x -> x.allitems.size() == 2);
    
    // repeat my Lunch to be every monday
    runFormula(executor, "(:s (: select (title (string Lunch))) (: update repeat (number 1)))", context, x -> x.allitems.size() == 3);
    
    // repeat my Lunch weekly
    runFormula(executor, "(:s (: select (title (string Lunch))) (: update repeat (number 0)))", context, x -> x.allitems.size() == 3);
    
    // clear all repeats from appointment
    runFormula(executor, "(:s (: select (title (string Appointment))) (: update repeat (number 11)))", context, x -> x.allitems.size() == 3);
    
    // cancel all meeting that repeat weekly
    runFormula(executor, "(:s (: select (repeat (number 0))) (: remove))", context, x -> x.allitems.size() == 2);
    
    // cancel all meeting that repeat on Thursdays
    runFormula(executor, "(:s (: select (repeat (number 4))) (: remove))", context, x -> x.allitems.size() == 2);
    
    
    
    // push meeting to tomorrow morning (free slot)
//    runFormula(executor, "(:s " + select_meeting + "(: free_slot (in_range start_datetime (call addtime (call today_start) (number 28 hours)) start_datetime (call addtime (call today_start) (number 32 hours)))))", context, x -> x.allitems.size() == 3);    
    
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
  
  @Test public void testBasicActions() {
    String defaultEvents = "["
    		+ "[\"Meeting_Pam\",\"office\",\"2016-07-22T10:15:00\",\"2016-07-22T11:15:00\",[0,0,0,0,0,0,0,0,0],[]],"
    		+ "[\"Group_Meeting\",\"office\",\"2016-07-21T10:15:00\",\"2016-07-22T17:15:00\",[0,0,0,0,0,0,0,0,0],[]],"
    		+ "[\"Lunch\",\"cafe\",\"2016-07-23T12:00:00\",\"2016-07-23T13:30:00\",[0,0,0,0,0,0,0,0,0],[]],"
    		+ "[\"Lunch\",\"bar\",\"2016-07-27T15:00:00\",\"2016-07-27T16:00:00\",[0,0,0,0,0,0,0,0,0],[]]"
    		+ "]";
    ContextValue context = getContext(defaultEvents);
    LogInfo.begin_track("testBasicActions");
    
    runFormula(executor, "(:s (: select *) (: remove) (: remove))", context, x -> x.allitems.size() == 0);
    runFormula(executor, "(:s (: select *) (: update title (string new_title)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (title (string Lunch))) (: remove))", context, x -> x.allitems.size() == 2);
    runFormula(executor, "(:s (: select (title (string Lunch))) (: update title (string replaced_TITLE)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (title (string Lunch))) (: update location (string replaced_LOCATION)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (title (string Lunch))) (: update start_weekday (string wed)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (title (string Lunch))) (: update end_weekday (string friday)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (title (string Lunch))) (: move start_weekday (string sat)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (title (string Lunch))) (: move end_weekday (string sun)))", context, x -> x.allitems.size() == 4);
    
    // new duration
    runFormula(executor, "(:s (: select *) (: update duration (number 90 minutes)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select *) (: update duration (number 3 hours)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (duration (number 90 minutes))) (: update duration (number 90 minutes)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(: select (duration (number 60 minutes)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(: select (duration (number 60 minutes)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (duration (number 60 minutes))) (: remove))", context, x -> x.allitems.size() == 2);
    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: remove))", context, x -> x.allitems.size() == 2);
    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: update duration (number 5 hours)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: update duration (number 30 minutes)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (duration (number 1 hours))) (: update duration (number 1.5 hours)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (duration (number 1.5 hours))) (: update duration (number 2.5 hours)))", context, x -> x.allitems.size() == 4);						
    						
    // date (update)
    runFormula(executor, "(:s (: select *) (: update start_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select *) (: update end_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select *) (: update start_date (date 1990 08 03)) (: update end_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (start_date (date 2016 07 21))) (: remove))", context, x -> x.allitems.size() == 3);
    runFormula(executor, "(:s (: select (end_date (date 2016 07 22))) (: remove))", context, x -> x.allitems.size() == 2);
    
    // date (move)
    runFormula(executor, "(:s (: select *) (: move start_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select *) (: move end_date (date 1990 08 03)))", context, x -> x.allitems.size() == 4);
    
    // time (update)
    runFormula(executor, "(:s (: select *) (: update start_time (time 13 30)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select *) (: update start_time (time 13 30)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select (start_time (time 10 15))) (: remove))", context, x -> x.allitems.size() == 2);
    runFormula(executor, "(:s (: select (end_time (time 13 30))) (: remove))", context, x -> x.allitems.size() == 3);
    
    // time (move)
    runFormula(executor, "(:s (: select *) (: move start_time (time 13 30)))", context, x -> x.allitems.size() == 4);
    runFormula(executor, "(:s (: select *) (: move end_time (time 13 30)))", context, x -> x.allitems.size() == 4);
    
    // new event
    runFormula(executor, "(: add)", context, x -> x.allitems.size() == 5);
    
  }
  
}