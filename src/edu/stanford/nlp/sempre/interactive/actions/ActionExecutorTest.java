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
 * @author Sida Wang
 */
public class ActionExecutorTest {
  ActionExecutor executor = new ActionExecutor();

  protected static void runFormula(ActionExecutor executor, String formula, ContextValue context, Predicate<FlatWorld> checker) {
    LogInfo.begin_track("formula: %s", formula);
    executor.opts.FlatWorldType = "BlocksWorld";
    Executor.Response response = executor.execute(Formulas.fromLispTree(LispTree.proto.parseFromString(formula)), context);
    
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph)context.graph;
    String wallString = ((StringValue)graph.triples.get(0).e1).value;
    String jsonStr = ((StringValue)response.value).value;
    LogInfo.logs("Start:\t%s", wallString);
    LogInfo.logs("Result:\t%s", jsonStr);
    LogInfo.end_track();
    
    if (checker != null) {
      if (!checker.test(FlatWorld.fromContext("BlocksWorld", getContext(jsonStr)))) {
        Assert.fail(jsonStr);
      }
    }
  }

  private static ContextValue getContext(String blocks) {
    // a hack to pass in the world state without much change to the code
    String strigify2 = Json.writeValueAsStringHard(blocks); // some parsing issue inside lisptree parser
    return ContextValue.fromString(String.format("(context (graph NaiveKnowledgeGraph ((string \"%s\") (name b) (name c))))", strigify2));
  }
  
  public Predicate<FlatWorld> selectedSize(int n) {
    return x -> {LogInfo.logs("Got %d, expected %d", x.selected().size(), n); return x.selected().size()==n;};
  }
  
  @Test public void testJoin() {
    String defaultBlocks = "[[1,1,1,\"Green\",[]],[1,2,1,\"Blue\",[]],[2,2,1,\"Red\",[]],[3,2,2,\"Yellow\",[]]]";
    ContextValue context = getContext(defaultBlocks);
    LogInfo.begin_track("testJoin");

    runFormula(executor, "(: select *)", context, selectedSize(4));
    runFormula(executor, "(: select (or (color red) (color green)))", context, selectedSize(2));
    runFormula(executor, "(: select (or (row (number 1)) (row (number 2))))", context, selectedSize(3));
    runFormula(executor, "(: select (col ((reverse row) (color red))))", context, null); // has same col as the row of color red
    runFormula(executor, "(: select (color ((reverse color) (row 3))))", context, null); // color of the color of cubes in row 3
    runFormula(executor, "(: select (color ((reverse color) (color ((reverse color) (color red))))))", context,
        x -> x.selected().iterator().next().get("color").equals("red"));
    runFormula(executor, "(: select (and (row 1) (not (color green))))", context,
        x -> x.selected().isEmpty());
    LogInfo.end_track();

  }
  @Test public void testSpecialSets() {
      String defaultBlocks = "[[1,1,1,\"Green\",[\"S\"]],[1,2,1,\"Blue\",[\"S\"]],[2,2,1,\"Red\",[\"S\"]],[2,2,2,\"Yellow\",[]]]";
      ContextValue context = getContext(defaultBlocks);
      LogInfo.begin_track("testSpecial");
      runFormula(executor, "(: select nothing)", context, selectedSize(0));
      runFormula(executor, "(: select *)", context, selectedSize(4));
      runFormula(executor, "(: select this)", context, selectedSize(3));
      runFormula(executor, "(: select (and this nothing))", context, selectedSize(0));
      runFormula(executor, "(: select (not this))", context, selectedSize(1));

      LogInfo.begin_track("testSpecial");;
  }
  
  @Test public void testMerge() {
    {
    String defaultBlocks = "[[1,1,1,\"Green\",[]],[1,2,1,\"Blue\",[]],[2,2,1,\"Red\",[]],[2,2,2,\"Yellow\",[]]]";
    ContextValue context = getContext(defaultBlocks);
    LogInfo.begin_track("testMerge");
    runFormula(executor, "(: select (or (color red) (color blue)))", context, selectedSize(2));
    runFormula(executor, "(: select (and (color red) (color blue)))", context, selectedSize(0));
    runFormula(executor, "(: select (not (color red)))", context, selectedSize(3));
    runFormula(executor, "(: select (not *))", context, selectedSize(0));
    } 
    LogInfo.end_track();
  }
  @Test public void testBasicActions() {
    String defaultBlocks = "[[1,1,1,\"Green\",[]],[1,2,1,\"Blue\",[]],[2,2,1,\"Red\",[]],[2,2,3,\"Yellow\",[]]]";
    ContextValue context = getContext(defaultBlocks);
    LogInfo.begin_track("testBasicActions");
    runFormula(executor, "(:s (: select *) (: remove) (: remove))", context, x -> x.allitems.size() == 0);
    runFormula(executor, "(:s (: select (row (number 1))) (: add red top) (: add red top))", context, x -> x.allitems.size() == 8);
    runFormula(executor, "(:for * (: remove))", context, x -> x.allitems.size() == 0);
    runFormula(executor, "(:foreach * (: remove))", context, x -> x.allitems.size() == 0);
    runFormula(executor, "(:s (: select (or (color red) (color orange))) (: remove))", context, x -> x.allitems.size() == 3);
    runFormula(executor, "(:foreach (or (color red) (color orange)) (:loop (number 5) (: add red top)))",
       context, x -> x.allitems.size() == 9);
    runFormula(executor, "(:for (or (color red) (color blue)) (:loop (number 5) (:s (: move left) (: move right) (: move left))))",
        context, null);
    runFormula(executor, "(:foreach (or (color red) (color blue)) (: update color ((reverse color) (call adj left))))",
        context, null);
    
    LogInfo.end_track();
  }
  
  @Test public void testMoreActions() {
    // this is a green stick
    String defaultBlocks = "[[1,1,1,\"Green\",[]],[1,1,2,\"Green\",[]],[1,1,3,\"Green\",[]],[1,1,4,\"Green\",[]]]";
    ContextValue context = getContext(defaultBlocks);
    LogInfo.begin_track("testMoreActions");
    runFormula(executor, "(:s (:for * (: select)) (:for (call veryx left this) (: remove)))", context, x -> x.allitems.size() == 0);
    runFormula(executor, "(:for * (:for (call veryx bot) (:loop (number 2) (:s (: add red left) (: select (call adj top))))))", context, 
        x -> x.allitems.size() == 6);
    runFormula(executor, "(:s (:for * (: select)) (: select (call veryx bot this)) (: remove) )", context, x -> x.allitems.size() == 3);
    // x -> x.selected().iterator().next().get("height") == new Integer(3)
    runFormula(executor, "(:loop (count (color green)) (: add red left *))", context, x -> x.allitems.size() == 20);

   LogInfo.end_track();
  }
  
  @Test public void troubleCases() {
    // this is a green stick
    String defaultBlocks = "[[1,1,1,\"Green\",[\"S\"]],[1,1,2,\"Green\",[]],[1,1,3,\"Green\",[]],[1,1,4,\"Green\",[]]]";
    ContextValue context = getContext(defaultBlocks);
    LogInfo.begin_track("troubleCases");
    runFormula(executor, "(:s (: select *) (: select (or (call veryx bot) (call veryx top))))", context, selectedSize(2));
    runFormula(executor, " (: select (or (call veryx top (color green)) (call veryx bot (color green))))", context, selectedSize(2));
    runFormula(executor, " (: select (and (call veryx top (color green)) (call veryx bot (color green))))", context, selectedSize(0));
    runFormula(executor, " (: select (call adj top this))", context, selectedSize(1));
    LogInfo.end_track();
  }
  
  @Test public void testAnchor() {
    // this is a green stick
    String defaultBlocks = "[[1,1,0,\"Anchor\",[\"S\"]]]";
    ContextValue context = getContext(defaultBlocks);
    LogInfo.begin_track("testAnchors");
    runFormula(executor, "(: add red top)", context, selectedSize(1));
    runFormula(executor, "(: add red left)", context, selectedSize(1));
    runFormula(executor, "(:loop (number 3) (: add red left))", context, selectedSize(1));
    runFormula(executor, "(:loop (number 3) (: add red top))", context, x -> x.allitems.size() == 3);
    LogInfo.end_track();
  }
  
  
}