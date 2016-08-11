package edu.stanford.nlp.sempre;

import fig.basic.LogInfo;

/**
 * Maps a string to a Unit.
 *
 * @author Nadav Lidor
 */
public class UnitFn extends SemanticFn {
  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
//        String value = ex.languageInfo.getNormalizedNerSpan("DURATION", c.getStart(), c.getEnd());
        String value = ex.phraseString(c.getStart(), c.getEnd());
        
        LogInfo.log("xxxxxxxxxxxxxxxx");
        LogInfo.log(value);
        
        if (value == null)
          return null;
        String unit = null;
        if (value.equalsIgnoreCase("minute") || value.equalsIgnoreCase("minutes")) unit = "minutes";
        else if (value.equalsIgnoreCase("hour") || value.equalsIgnoreCase("hours")) unit = "hours";
        else if (value.equalsIgnoreCase("day") || value.equalsIgnoreCase("days")) unit = "days";
        else if (value.equalsIgnoreCase("week") || value.equalsIgnoreCase("weeks")) unit = "weeks";
        else if (value.equalsIgnoreCase("month") || value.equalsIgnoreCase("months")) unit = "months";
        
        if (unit == null)
        	return null;
        
        LogInfo.log("yyyyyyyyyyyyyyy");
        LogInfo.log(unit);
        
        return new Derivation.Builder()
                .withCallable(c)
                .formula(new ValueFormula<>(new StringValue(unit)))
                .type(SemType.stringType)
                .createDerivation();
      }
    };
  }
}


