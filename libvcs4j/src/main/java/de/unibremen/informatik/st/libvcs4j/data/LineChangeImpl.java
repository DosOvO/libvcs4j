package de.unibremen.informatik.st.libvcs4j.data;

import de.unibremen.informatik.st.libvcs4j.LineChange;
import de.unibremen.informatik.st.libvcs4j.VCSFile;
import de.unibremen.informatik.st.libvcs4j.Validate;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Pojo implementation of {@link LineChange}.
 */
@Getter
@Setter
@ToString(of = {"type", "line", "file"}, doNotUseGetters = true)
public class LineChangeImpl extends VCSModelElementImpl implements LineChange {

	/**
	 * The type of a line change.
	 */
	@NonNull
	private Type type;

	/**
	 * The line number of a line change.
	 */
	private int line;

	/**
	 * The content of a line change.
	 */
	@NonNull
	private String content;

	/**
	 * The file a line change belongs to.
	 */
	@NonNull
	private VCSFile file;

	/**
	 * Sets the line number of this line change.
	 *
	 * @param pLine
	 * 		The line number to set.
	 * @throws IllegalArgumentException
	 * 		If {@code pLine < 1}.
	 */
	public void setLine(final int pLine) {
		Validate.isTrue(pLine >= 1, "line < 1");
		line = pLine;
	}
}
