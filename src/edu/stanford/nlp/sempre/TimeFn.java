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
//        LogInfo.log("TimeFn value1 :" + value);
//        String value2 = ex.languageInfo.tokens.get(c.getStart());
//        LogInfo.log("TimeFn value2 :" + value2);
        if (value == null)
          return null;
        TimeValue timeValue = TimeValue.parseTimeValue(value);
        if (timeValue == null)
          return null;
        return new Derivation.Builder()
                .withCallable(c)
                .formula(new ValueFormula<>(timeValue))
                .type(SemType.timeType)
                .createDerivation();
      }
    };
  }
}


