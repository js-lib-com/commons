package js.lang;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Immutable list of string pairs. Instances of this list are constructed from a string expression. Given expression
 * should be a list of semicolon separated pairs, every pair consisting of two values separated by separator. Pairs
 * order from expression is preserved.
 * <p>
 * Here is supported pairs list syntax. Please note that final semicolon is optional.
 * 
 * <pre>
 *    expression = pair *( ";" pair ) [ ";" ]
 *    pair = first SEP second
 *    first = string
 *    second = string
 *    
 *    ; separator for pair components 
 *    SEP = ":" / "="
 *    
 *    ; string is any character less semicolon and separators
 *    ; colon is default separator
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public class PairsList implements Iterable<Pair>
{
  /** Default pair separator. */
  private static final char DEFAULT_SEPARATOR = ':';

  /** Underlying pairs storage. */
  private final List<Pair> pairs = new ArrayList<>();

  /**
   * Convenient constructor using default separator.
   * 
   * @param expression expression to parse.
   * @throws SyntaxException if <code>expression</code> is not well formed or on duplicated pairs.
   */
  public PairsList(String expression) throws SyntaxException
  {
    this(expression, DEFAULT_SEPARATOR);
  }

  /**
   * Parse expression and extract a list of pairs of string values. See this class description for expression syntax.
   * Please note that <code>expression</code> values should not contain semicolon used for pairs separation or used
   * values <code>separator</code>.
   * 
   * @param expression expression to parse,
   * @param separator separator character.
   * @throws SyntaxException if <code>expression</code> is not well formed or on duplicated pairs.
   */
  public PairsList(String expression, char separator) throws SyntaxException
  {
    int semicolonIndex = 0;
    for(;;) {
      int colonIndex = expression.indexOf(separator, semicolonIndex);
      if(colonIndex == -1) {
        break;
      }
      String firstValue = expression.substring(semicolonIndex, colonIndex);

      ++colonIndex;
      semicolonIndex = expression.indexOf(';', colonIndex);
      if(semicolonIndex == -1) {
        semicolonIndex = expression.length();
      }
      if(colonIndex == semicolonIndex) {
        throw new SyntaxException("Invalid pairs expression |%s|. First value is empty.", expression);
      }

      Pair pair = new Pair(firstValue, expression.substring(colonIndex, semicolonIndex));
      if(pairs.contains(pair)) {
        throw new SyntaxException("Duplicated pairs in expression |%s|.", expression);
      }
      pairs.add(pair);
      ++semicolonIndex;
    }

    if(pairs.isEmpty()) {
      throw new SyntaxException("Invalid pairs expression |%s|. Missing pair separator |%s|.", expression, separator);
    }
  }

  /**
   * Returns an iterator for this pairs list instance.
   * 
   * @return pairs iterator.
   */
  @Override
  public Iterator<Pair> iterator()
  {
    return pairs.iterator();
  }
}
