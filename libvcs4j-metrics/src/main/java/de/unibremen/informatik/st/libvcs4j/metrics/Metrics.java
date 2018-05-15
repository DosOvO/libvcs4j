package de.unibremen.informatik.st.libvcs4j.metrics;

import de.unibremen.informatik.st.libvcs4j.VCSFile;
import org.apache.commons.lang3.Validate;
import org.conqat.lib.scanner.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides methods to calculate different metrics.
 */
@SuppressWarnings("unused")
public class Metrics {

	/**
	 * Tries to compute the size of the given file.
	 *
	 * @param pFile
	 * 		The file to compute the size for.
	 * @return
	 * 		The size of {@code pFile}.
	 * @throws NullPointerException
	 * 		If {@code pFile} is {@code null}.
	 * @throws IOException
	 * 		If an error occurred while reading the contents of {@code pFile}.
	 */
	public static Optional<Size> computeSize(final VCSFile pFile)
			throws NullPointerException, IOException {
		Validate.notNull(pFile);

		final Optional<ELanguage> maybeLang = getLanguage(pFile);
		if (!maybeLang.isPresent()) {
			return Optional.empty();
		}

		final ELanguage lang = maybeLang.get();
		final String content = pFile.readeContent();
		final IScanner scanner = ScannerFactory
				.newLenientScanner(lang, content, "");

		final String LINE_SEP = "\\r?\\n";
		int loc = 0, sloc = 0, cloc = 0, not = 0, snot = 0, cnot = 0;
		IToken lastToken = null;
		try {
			for (IToken token = scanner.getNextToken();
				 token.getType() != ETokenType.EOF;
				 lastToken = token, token = scanner.getNextToken()) {
				not++;
				final boolean comment = isCommentType(token);
				if (comment) {
					cnot++;
				} else {
					snot++;
				}
				if (lastToken == null) {
					if (comment) {
						cloc = 1;
					} else {
						sloc = 1;
					}
				} else {
					final String ltText = content.substring(
							lastToken.getOffset(),
							lastToken.getEndOffset() + 1);
					final int ltEndLine = lastToken.getLineNumber() +
							ltText.split(LINE_SEP).length - 1;
					final String text = content.substring(
							token.getOffset(),
							token.getEndOffset() + 1);
					int linesToAdd = text.split(LINE_SEP).length;
					if (ltEndLine == token.getLineNumber()) {
						linesToAdd--;
					}
					if (comment) {
						cloc += linesToAdd;
					} else {
						sloc += linesToAdd;
					}
				}
			}
		} catch (final ScannerException e) {
			throw new IOException(e);
		}
		if (lastToken != null) {
			final String text = content.substring(
					lastToken.getOffset(),
					lastToken.getEndOffset() + 1);
			final int endLine = lastToken.getLineNumber()
					+ text.split(LINE_SEP).length - 1;
			loc = endLine + 1;
		}

		final Size size = new Size(loc, sloc, cloc, not, snot, cnot);
		return Optional.of(size);
	}

	/**
	 * Tries to compute the complexity of the given file.
	 *
	 * @param pFile
	 * 		The file to compute the complexity for.
	 * @return
	 * 		The complexity of {@code pFile}.
	 * @throws NullPointerException
	 * 		If {@code pFile} is {@code null}.
	 * @throws IOException
	 * 		If an error occurred while reading the contents of {@code pFile}.
	 */
	public static Optional<Complexity> computeComplexity(final VCSFile pFile)
			throws NullPointerException, IOException {
		Validate.notNull(pFile);

		final Optional<ELanguage> maybeLang = getLanguage(pFile);
		if (!maybeLang.isPresent()) {
			return Optional.empty();
		}

		final ELanguage lang = maybeLang.get();
		final String content = pFile.readeContent();
		final IScanner scanner = ScannerFactory
				.newLenientScanner(lang, content, "");

		final List<IToken> operators = new ArrayList<>();
		final List<IToken> operands = new ArrayList<>();
		int mccabe = 1;
		try {
			for (IToken token = scanner.getNextToken();
				 token.getType() != ETokenType.EOF;
				 token = scanner.getNextToken()) {
				if (isCommentType(token)) {
					continue;
				} else if (token.getType().isError()) {
					continue;
				} else if (token.getType().isOperator()) {
					operators.add(token);
				} else {
					operands.add(token);
				}

				if (isControlType(token)) {
					mccabe++;
				}
			}
		} catch (final ScannerException e) {
			throw new IOException(e);
		}
		final List<String> distinctOperators =
				operators.stream()
						.map(IToken::getText)
						.distinct()
						.collect(Collectors.toList());
		final List<String> distinctOperands =
				operands.stream()
						.map(IToken::getText)
						.distinct()
						.collect(Collectors.toList());

		final Complexity.Halstead halstead =
				new Complexity.Halstead(
						distinctOperators.size(),
						distinctOperands.size(),
						operators.size(),
						operands.size());
		final Complexity complexity = new Complexity(mccabe, halstead);
		return Optional.of(complexity);
	}

	/**
	 * Returns whether the given token is a comment.
	 *
	 * @param pToken
	 * 		The token to check.
	 * @return
	 * 		{@code true} if {@code pToken} is a comment, {@code false}
	 * 		otherwise.
	 */
	private static boolean isCommentType(final IToken pToken) {
		final ETokenType type = pToken.getType();
		return type == ETokenType.COMMENT ||
				type == ETokenType.COMMENT_KEYWORD ||
				type == ETokenType.DOCUMENTATION_COMMENT ||
				type == ETokenType.TRADITIONAL_COMMENT ||
				type == ETokenType.END_OF_LINE_COMMENT ||
				type == ETokenType.HASH_COMMENT;
	}

	/**
	 * Returns whether the given token is a control flow.
	 *
	 * @param pToken
	 * 		The token to check.
	 * @return
	 * 		{@code true} if {@code pToken} is a control flow, {@code false}
	 * 		otherwise.
	 */
	private static boolean isControlType(final IToken pToken) {
		final ETokenType type = pToken.getType();
		return
				// if condition
				type == ETokenType.IF ||
						type == ETokenType.IFN ||
						type == ETokenType.ANDIF ||
						type == ETokenType.ORIF ||
						type == ETokenType.ELIF ||
						type == ETokenType.ELSIF ||
						type == ETokenType.ELSEIF ||
						type == ETokenType.MODIF ||
						type == ETokenType.NULLIF ||
				// is condition
						type == ETokenType.IS_DATE ||
						type == ETokenType.IS_EMPTY ||
						type == ETokenType.IS_FLOAT ||
						type == ETokenType.ISOLATION ||
						type == ETokenType.IS_NOT ||
						type == ETokenType.IS_NUMBER ||
						type == ETokenType.IS_TIME ||
				// for loop
						type == ETokenType.FOR ||
						type == ETokenType.FORALL ||
						type == ETokenType.FOREACH ||
				// while loop
						type == ETokenType.WHILE ||
				// try block
						type == ETokenType.TRY;
	}

	/**
	 * Tries to guess the language of the given file.
	 *
	 * @param file
	 * 		The file to guess the language for.
	 * @return
	 * 		The language of {@code pFile} or an empty {@link Optional} if the
	 * 		language is unknown.
	 */
	private static Optional<ELanguage> getLanguage(final VCSFile file) {
		final ELanguage l = ELanguage.fromFile(file.toFile());
		return l == null || l == ELanguage.TEXT || l == ELanguage.LINE
				? Optional.empty()
				: Optional.of(l);
	}
}