package edu.stanford.nlp.sempre;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Maps a string to a Date.
 *
 * @author Percy Liang
 */
public class DateFn extends SemanticFn {
	
//	public static final String[] TO_FILTER = new String[] { "today", "tomorrow" }; TODO  
//	public static final Set<String> FILTER_SET = new HashSet<String>(Arrays.asList(TO_FILTER));
	
  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        String value = ex.languageInfo.getNormalizedNerSpan("DATE", c.getStart(), c.getEnd());
        if (value == null)
          return null;
//      	String line = ex.phraseString(c.getStart(), c.getEnd()); // todo     
//      	if (FILTER_SET.contains(line)) 
//      		return null;
        DateValue dateValue = DateValue.parseDateValue(value);
        if (dateValue == null)
          return null;
        return new Derivation.Builder()
                .withCallable(c)
                .formula(new ValueFormula<>(dateValue))
                .type(SemType.dateType)
                .createDerivation();
      }
    };
  }
}
