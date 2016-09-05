package edu.stanford.nlp.sempre;

import fig.basic.LogInfo;

/**
 * Maps a string to a Time.
 *
 * @author Nadav Lidor
 */
public class SUDateTimeFn extends SemanticFn {
  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        String value = ex.languageInfo.getNormalizedNerSpan("TIME", c.getStart(), c.getEnd());
        if (value == null) value = ex.languageInfo.getNormalizedNerSpan("DATE", c.getStart(), c.getEnd());
        if (value == null) return null;
        SUDateTimeValue suDateTimeValue = SUDateTimeValue.checkDateTimeValue(value);
        if (suDateTimeValue == null)
          return null;
        return new Derivation.Builder()
                .withCallable(c)
                .formula(new ValueFormula<>(suDateTimeValue))
                .type(SemType.dateType)
                .createDerivation();
      }
    };
  }
}


