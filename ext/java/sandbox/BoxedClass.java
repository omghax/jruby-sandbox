package sandbox;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.Block;

@JRubyClass(name="BoxedClass")
public class BoxedClass {
  protected static RubyClass createBoxedClassClass(final Ruby runtime) {
    RubyClass cObject = runtime.getObject();
    RubyClass cBoxedClass = runtime.defineClass("BoxedClass", cObject, cObject.getAllocator());
    cBoxedClass.defineAnnotatedMethods(BoxedClass.class);

    return cBoxedClass;
  }

  @JRubyMethod(module=true, rest=true)
  public static IRubyObject method_missing(IRubyObject recv, IRubyObject[] args, Block block) {
    IRubyObject[] args2 = new IRubyObject[args.length - 1];
    System.arraycopy(args, 1, args2, 0, args2.length);
    String name = args[0].toString();

    SandboxFull box = (SandboxFull) SandboxFull.getLinkedBox(recv);
    return box.runMethod(recv, name, args2, block);
  }

  @JRubyMethod(name="new", meta=true, rest=true)
  public static IRubyObject _new(IRubyObject recv, IRubyObject[] args, Block block) {
    SandboxFull box = (SandboxFull) SandboxFull.getLinkedBox(recv);
    return box.runMethod(recv, "new", args, block);
  }
}
