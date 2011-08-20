package sandbox;

import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.builtin.IRubyObject;

@JRubyClass(name="BoxedClass")
public class BoxedClass {
  @JRubyMethod(module=true, rest=true)
  public static IRubyObject method_missing(IRubyObject recv, IRubyObject[] args) {
    IRubyObject[] args2 = new IRubyObject[args.length - 1];
    System.arraycopy(args, 1, args2, 0, args2.length);
    String name = args[0].toString();

    SandboxFull box = (SandboxFull) SandboxFull.getLinkedBox(recv);
    return box.runMethod(recv, name, args2);
  }

  @JRubyMethod(name="new", meta=true, rest=true)
  public static IRubyObject _new(IRubyObject recv, IRubyObject[] args) {
    SandboxFull box = (SandboxFull) SandboxFull.getLinkedBox(recv);
    return box.runMethod(recv, "new", args);
  }
}
