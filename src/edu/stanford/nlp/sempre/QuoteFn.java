package edu.stanford.nlp.sempre;

import fig.basic.LogInfo;

/**
 * Maps a string to a Date.
 *
 * @author Nadav Lidor
 */
public class QuoteFn extends SemanticFn {
	
  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        String value = ex.phraseString(c.getStart(), c.getEnd());
        LogInfo.log("QuoteFn: " + value);
        if (value == null || value.length() < 2 || value.charAt(0) != '\"' || value.charAt(value.length() - 1) != '\"')
          return null;
        StringValue stringValue = new StringValue(value);  
        return new Derivation.Builder()
                .withCallable(c)
                .formula(new ValueFormula<>(stringValue))
                .type(SemType.stringType)
                .createDerivation();
      }
    };
  }
}
