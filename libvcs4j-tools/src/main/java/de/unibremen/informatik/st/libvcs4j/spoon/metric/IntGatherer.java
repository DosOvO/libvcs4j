package de.unibremen.informatik.st.libvcs4j.spoon.metric;

import de.unibremen.informatik.st.libvcs4j.Validate;

/**
 * A metric gatherer of type {@link Integer}.
 */
public abstract class IntGatherer extends Gatherer<Integer> {

	@Override
	protected Integer sum(final Integer a, final Integer b)
			throws NullPointerException {
		return Validate.notNull(a) + Validate.notNull(b);
	}

	/**
	 * Increments the metric of the top element of {@link #stack} by 1.
	 */
	void inc() {
		inc(1);
	}
}
