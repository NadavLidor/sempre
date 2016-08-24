package edu.stanford.nlp.sempre;

import fig.basic.LogInfo;

/**
 * Maps a string to a Time.
 *
 * @author Nadav Lidor
 */
public class TimeFn extends SemanticFn {
  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        String value = ex.languageInfo.getNormalizedNerSpan("TIME", c.getStart(), c.getEnd());
        if (value != null) {
	        TimeValue timeValue = TimeValue.parseTimeValue(value);
	        if (timeValue == null)
	          return null;
	        return new Derivation.Builder()
	                .withCallable(c)
	                .formula(new ValueFormula<>(timeValue))
	                .type(SemType.timeType)
	                .createDerivation();
        }
        return null;
//        else { // looking forward option TODO not for now
//          value = ex.languageInfo.getNormalizedNerSpan("NUMBER", c.getStart(), c.getEnd());
//          LogInfo.log("TimeFn.else: " + value);
//          if (value != null) {
//	        TimeValue timeValue = TimeValue.parseTimeNumberValue(value);
//	        if (timeValue == null)
//	          return null;
//	          LogInfo.log("TimeFn timeValue :" + timeValue);
//	        return new Derivation.Builder()
//	                .withCallable(c)
//	                .formula(new ValueFormula<>(timeValue))
//	                .type(SemType.timeType)
//	                .createDerivation();
//        }
      }
    };
  }
}


