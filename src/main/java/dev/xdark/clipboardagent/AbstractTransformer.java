package dev.xdark.clipboardagent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

abstract class AbstractTransformer implements ClassFileTransformer {
	boolean pendingPatch = true;

	abstract String className();

	abstract void apply(ClassReader cr, ClassWriter cw);

	@Override
	public final byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		String internalName = className().replace('.', '/');
		if (!internalName.equals(className)) return null;
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
			@Override
			protected ClassLoader getClassLoader() {
				return null;
			}
		};
		try {
			apply(cr, cw);
		} catch (Exception ex) {
			PrintStream err = System.err;
			synchronized (err) {
				err.printf("Failed to apply the patch to %s%n", className());
				ex.printStackTrace(err);
			}
			return null;
		}
		byte[] result = cw.toByteArray();
		return result;
	}
}
