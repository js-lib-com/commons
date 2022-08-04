package js.io;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * Character stream writer with variable injection. This is a standard characters stream but with variables using standard
 * <code>${...}</code> notation. Values are injected on the fly, while the character stream is written to target writer. This
 * class gets a variables map from where retrieve values by name. If a variable from stream has a name not existing into
 * variables map, stream variable remains unresolved.
 * 
 * <pre>
 * +----------+   (1)   +-----------------------+   (2)   +---------------+
 * | template +---------&gt; VariablesWriter.write +---------&gt; target writer |
 * +----------+         +-----------------------+         +---------------+
 * 
 * 1 - variables stream
 * 2 - stream with values injected
 * </pre>
 * 
 * <p>
 * Variables writer is in fact a decorator. It adds variable injection functionality to an ordinary writer. If none provided on
 * constructor uses an internal {@link StringWriter}.
 * 
 * <pre>
 * // variables writer with string target writer
 * VariablesWriter writer = new VariablesWriter(variables);
 * // copy template and inject values on the fly
 * Files.copy(new StringReader(template), writer);
 * // string with values resolved
 * String string = writer.toString();
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public class VariablesWriter extends Writer {
	/** Target writer is where stream with variables injected is actually being write. */
	private Writer targetWriter;
	/** Variable values map. */
	private Map<String, String> variables;
	/** String builder for variable name discovered on variables writer. */
	private StringBuilder variableBuilder;
	/** Current state of the finite states machine used by variables parser. */
	private State state;

	/**
	 * Construct a variables writer instance targeting an internal string writer. Its value can be retrieved via this class
	 * {@link #toString()}.
	 * 
	 * @param variables variables map.
	 */
	public VariablesWriter(Map<String, String> variables) {
		this(new StringWriter(), variables);
	}

	/**
	 * Construct a variables writer decorator for given target writer.
	 * 
	 * @param targetWriter target writer,
	 * @param variables variables map.
	 */
	public VariablesWriter(Writer targetWriter, Map<String, String> variables) {
		super();
		this.targetWriter = targetWriter;
		this.variables = variables;
		this.variableBuilder = new StringBuilder();
		this.state = State.TEXT;
	}

	/**
	 * Inject values into given variables stream and write the result to target writer.
	 * 
	 * @param cbuf characters from variables stream,
	 * @param off buffer offset,
	 * @param len buffer length.
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int count = 0; count < len; ++off, ++count) {
			char c = cbuf[off];

			switch (state) {
			case TEXT:
				if (c == '$') {
					state = State.VARIABLE_MARK;
				} else {
					targetWriter.write(c);
				}
				break;

			case VARIABLE_MARK:
				if (c == '{') {
					state = State.VARIABLE;
					variableBuilder.setLength(0);
				} else {
					targetWriter.write('$');
					targetWriter.write(c);
					state = State.TEXT;
				}
				break;

			case VARIABLE:
				if (c == '}') {
					String variable = variables.get(variableBuilder.toString());
					if (variable == null) {
						// if variable not found it should be an internal variable used by processing file
						// for example ${BUILD} from ant files; copy as it is
						targetWriter.write("${");
						targetWriter.write(variableBuilder.toString());
						targetWriter.write('}');
					} else {
						targetWriter.write(variable);
					}
					state = State.TEXT;
				} else {
					variableBuilder.append(c);
				}
				break;

			default:
				throw new IllegalStateException();
			}
		}
	}

	/** Flush target writer. */
	@Override
	public void flush() throws IOException {
		targetWriter.flush();
	}

	/** Close target writer. */
	@Override
	public void close() throws IOException {
		targetWriter.close();
	}

	/** Return target writer string value. Useful if target writer is a string writer. */
	@Override
	public String toString() {
		return targetWriter.toString();
	}

	/**
	 * Internal state machine for variables writer parser.
	 * 
	 * @author Iulian Rotaru
	 */
	private static enum State {
		/** Neutral value */
		NONE,
		/** Inside ordinary text, that is, not variable. */
		TEXT,
		/** Variable mark discovered. */
		VARIABLE_MARK,
		/** Variable in process. */
		VARIABLE
	}
}
