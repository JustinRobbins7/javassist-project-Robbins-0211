package hw07;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import util.UtilMenu;

public class MethodSubber extends ClassLoader {

	static String _L_ = System.lineSeparator();

	public static void main(String[] args) {

		ArrayList<String> moddedMethods = new ArrayList<String>();
		while (true) {
			String[] input;
			boolean repeat = false;
			do {
				System.out.println("===================================================================");
				System.out.println("HW07 - Please enter a class, an method name, a parameter index,    ");
				System.out.println("and a number to replace that parameter, separated by commas.       ");
				System.out.println("===================================================================");

				input = UtilMenu.getArguments();
				repeat = false;

				if (input.length != 4) {
					repeat = true;
					System.out.println("[WRN] Invalid Input!");
				}

				if (input.length >= 1 && moddedMethods.contains(input[0])) {
					repeat = true;
					System.out.println("[WRN] This method " + input[0] + " has been modified!");
				}
			} while (repeat);

			String classname = "target." + input[0];
			String mname = input[1];
			String pmindex = input[2];
			String newpmv = input[3];

			try {
				MethodSubber ms = new MethodSubber(classname, mname, pmindex, newpmv);
				Class<?> c = ms.findClass(classname);
				Method mainMethod = c.getDeclaredMethod("main", new Class[] { String[].class });
				mainMethod.invoke(null, new Object[] { args });
			} catch (Exception e) {
				e.printStackTrace();
			}

			moddedMethods.add(input[0]);
		}
	}

	private ClassPool pool;
	private String classname;
	private String mname;
	private String pmindex;
	private String newpmv;

	public MethodSubber() throws NotFoundException {
		pool = new ClassPool();
		pool.insertClassPath(new ClassClassPath(new java.lang.Object().getClass()));
		//System.out.println("[DBG] Class Pathes: " + pool.toString());
	}

	public MethodSubber(String classn, String method, String index, String param) throws NotFoundException {
		pool = new ClassPool();
		pool.insertClassPath(new ClassClassPath(new java.lang.Object().getClass()));
		//System.out.println("[DBG] Class Pathes: " + pool.toString());
		this.classname = classn;
		this.mname = method;
		this.pmindex = index;
		this.newpmv = param;
	}

	/*
	 * Finds a specified class. The bytecode for that class can be modified.
	 */
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		CtClass cc = null;
		try {
			cc = pool.get(name);
			if (!cc.getName().equals(this.classname)) {
				return defineClass(name, cc.toBytecode(), 0, cc.toBytecode().length);
			}
			String classn = this.classname;
			String m = this.mname;
			String pind = this.pmindex;
			String npm = this.newpmv;

			cc.instrument(new ExprEditor() {
				public void edit(MethodCall call) throws CannotCompileException {
					String className = call.getClassName();
					String methodName = call.getMethodName();

					if (className.equals(classn) && methodName.equals(m)) {
						System.out.println("[Edited by ClassLoader] method name: " + methodName + ", line: "
								+ call.getLineNumber());
						String block1 = "{" + _L_ //
		                        + "System.out.println(\"\tReset param " + pind + " to " + npm + ".\"); " + _L_ //
								+ "$" + pind + "=" + npm + "; " + _L_ //
								+ "$proceed($$); " + _L_ //
								+ "}";
						//System.out.println("[DBG] BLOCK1: " + block1);
						//System.out.println("------------------------");
						call.replace(block1);
					}
				}
			});
			byte[] b = cc.toBytecode();
			return defineClass(name, b, 0, b.length);
		} catch (NotFoundException e) {
			throw new ClassNotFoundException();
		} catch (IOException e) {
			throw new ClassNotFoundException();
		} catch (CannotCompileException e) {
			e.printStackTrace();
			throw new ClassNotFoundException();
		}
	}
}
