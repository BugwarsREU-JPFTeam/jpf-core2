package gov.nasa.jpf;

import java.lang.reflect.Constructor;

public class SerializationConstructor<T> extends Constructor<T> {

	// those are set by the the sun.reflect.ReflectionFactory
	Class<T> mdc;
	Constructor<?> firstNonSerializableCtor;

	@Override
	public Class<T> getDeclaringClass() {
		return mdc;
	}

	// this has to be native because we need to create a new object of the
	// mdc without initializing it
	@Override
	public native T newInstance(Object... args);

	@Override
	public boolean isSynthetic() {
		return true;
	}

}
