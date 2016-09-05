package edu.stanford.nlp.sempre;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.sempre.interactive.actions.EventsWorld;
import fig.basic.LogInfo;

/**
 * Maps a string to a Date.
 *
 * @author Percy Liang
 */
public class SUDateFn extends SemanticFn {
	
  public DerivationStream call(final Example ex, final Callable c) {
    return new SingleDerivationStream() {
      @Override
      public Derivation createDerivation() {
        String value = ex.languageInfo.getNormalizedNerSpan("DATE", c.getStart(), c.getEnd());
        
        if (value == null)
          return null;
        
        if (value.contains(" T1") || value.contains(" T0") || value.equals("PRESENT_REF")) // don't handle datetime 
        	return null;
        
      	SUDateValue suDateValue = new SUDateValue(value);
        LogInfo.log("SUDateFn value: " + value);
        return new Derivation.Builder()
            .withCallable(c)
            .formula(new ValueFormula<>(suDateValue))
            .type(SemType.dateType)
            .createDerivation();
      }
    };
  }
}
