package edu.stanford.nlp.sempre;

import fig.basic.LogInfo;

/**
 * Maps a string to a Time.
 *
 * @author Nadav Lidor
 */
public class DateTimeFn extends SemanticFn {
  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        String value = ex.languageInfo.getNormalizedNerSpan("TIME", c.getStart(), c.getEnd());
//        LogInfo.log("dateTimeValue value1 :" + value);
//        String value2 = ex.languageInfo.tokens.get(c.getStart());
//        LogInfo.log("TimeFn value2 :" + value2);
        if (value == null)
          return null;
        DateTimeValue dateTimeValue = DateTimeValue.parseDateTimeValue(value);
//        LogInfo.log("dateTimeValue dateTimeValue :" + dateTimeValue);
        if (dateTimeValue == null)
          return null;
        return new Derivation.Builder()
                .withCallable(c)
                .formula(new ValueFormula<>(dateTimeValue))
                .type(SemType.timeType)
                .createDerivation();
      }
    };
  }
}


