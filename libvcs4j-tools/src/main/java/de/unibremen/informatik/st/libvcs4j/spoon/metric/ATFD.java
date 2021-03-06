package de.unibremen.informatik.st.libvcs4j.spoon.metric;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;

import java.util.Optional;

/**
 * This scanner gathers the 'Access to Foreign Data' metric for {@link CtEnum},
 * {@link CtClass}, and {@link CtInterface} elements.
 */
public class ATFD extends IntGatherer {

	/**
	 * The initial metric value.
	 */
	public static final int INITIAL_VALUE = 0;

	@Override
	public String name() {
		return "Access to Foreign Data";
	}

	@Override
	public String abbreviation() {
		return "ATFD";
	}

	/**
	 * Returns the 'Access to Foreign Data' metric of {@code type}. Returns an
	 * empty {@link Optional} if {@code type} is {@code null}, or if
	 * {@code type} was not scanned.
	 *
	 * @param type
	 * 		The type whose 'Access to Foreign Data' metric is requested.
	 * @return
	 * 		The 'Access to Foreign Data' metric of {@code type}.
	 */
	public Optional<Integer> ATFDOf(final CtType type) {
		return metricOf(type);
	}

	@Override
	public <T> void visitCtClass(final CtClass<T> ctClass) {
		visitNode(ctClass, super::visitCtClass,
				Integer::sum, INITIAL_VALUE);
	}

	@Override
	public <T> void visitCtInterface(final CtInterface<T> ctInterface) {
		visitNode(ctInterface, super::visitCtInterface,
				Integer::sum, INITIAL_VALUE);
	}

	@Override
	public <T extends Enum<?>> void visitCtEnum(final CtEnum<T> ctEnum) {
		visitNode(ctEnum, super::visitCtEnum,
				Integer::sum, INITIAL_VALUE);
	}

	@Override
	public <T> void visitCtFieldRead(final CtFieldRead<T> fieldRead) {
		visitCtFieldAccess(fieldRead);
		super.visitCtFieldRead(fieldRead);
	}

	@Override
	public <T> void visitCtFieldWrite(final CtFieldWrite<T> fieldWrite) {
		visitCtFieldAccess(fieldWrite);
		super.visitCtFieldWrite(fieldWrite);
	}

	private void visitCtFieldAccess(final CtFieldAccess fieldAccess) {
		final Optional<CtField> field = Optional.of(fieldAccess)
				.map(CtFieldAccess::getVariable)
				.map(CtFieldReference::getDeclaration);
		if (!field.isPresent() || // An field that is not part of the
				                  // classpath => foreign access.
				!isInScopeOf(field.get(),
						fieldAccess.getParent(CtType.class))) {
			inc();
		}
	}

	@Override
	public <T> void visitCtInvocation(final CtInvocation<T> invocation) {
		if (isFieldAccess(invocation)) {
			final Optional<CtMethod> method = resolveToMethod(invocation);
			if (!method.isPresent() || // A method that is not part of the
					                   // classpath => foreign access.
					!isInScopeOf(method.get(),
							invocation.getParent(CtType.class))) {
				inc();
			}
		}
		super.visitCtInvocation(invocation);
	}
}
