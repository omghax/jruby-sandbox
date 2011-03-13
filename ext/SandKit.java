import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyClass;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyKernel;
import org.jruby.RubyModule;
import org.jruby.RubyObject;

import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.anno.JRubyModule;

import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;

import org.jruby.common.IRubyWarnings;

import org.jruby.exceptions.RaiseException;

@JRubyClass(name="Sandbox::Kit")
public class SandKit extends RubyObject {
  private final static ObjectAllocator KIT_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new SandKit(runtime, klass);
    }
  };

  public static void initialize(Ruby runtime) {
    rb_mSandbox = runtime.defineModule("Sandbox");

    rb_cSandboxKit = rb_mSandbox.defineClassUnder("Kit", runtime.getObject(), KIT_ALLOCATOR);
    rb_cSandboxKit.defineAnnotatedMethods(SandKit.class);

    rb_eSandboxException = rb_mSandbox.defineClassUnder("SandboxException", runtime.getStandardError(), runtime.getStandardError().getAllocator());
  }

  // TODO remove these
  private static RubyModule rb_mSandbox;
  private static RubyClass rb_cSandboxKit;
  private static RubyClass rb_eSandboxException;

  private Ruby runtime;
  private Ruby wrapped;

  public SandKit(Ruby runtime, RubyClass klass) {
    super(runtime, klass);
    this.runtime = runtime;
    reset();
  }

  @JRubyMethod
  public IRubyObject reset() {
    this.wrapped = initWrapped();
    return this;
  }

  private Ruby initWrapped() {
    RubyInstanceConfig cfg = new RubyInstanceConfig();
    cfg.setObjectSpaceEnabled(runtime.getInstanceConfig().isObjectSpaceEnabled());
    cfg.setInput(runtime.getInstanceConfig().getInput());
    cfg.setOutput(runtime.getInstanceConfig().getOutput());
    cfg.setError(runtime.getInstanceConfig().getError());
    return Ruby.newInstance(cfg);
  }

  @JRubyMethod(required=2)
  public IRubyObject keep_methods(IRubyObject class_name, IRubyObject methods) {
    String className = class_name.asJavaString();
    RubyModule module = wrapped.getModule(className);
    if (module != null) {
      keepMethods(module, methods.convertToArray());
    }
    return methods;
  }

  @JRubyMethod(required=2)
  public IRubyObject keep_singleton_methods(IRubyObject class_name, IRubyObject methods) {
    String className = class_name.asJavaString();
    RubyModule module = wrapped.getModule(className).getSingletonClass();
    if (module != null) {
      keepMethods(module, methods.convertToArray());
    }
    return methods;
  }

  private void keepMethods(RubyModule module, Collection retain) {
    for (Map.Entry<String, DynamicMethod> methodEntry : module.getMethods().entrySet()) {
      String methodName = methodEntry.getKey();
      if (!retain.contains(methodName)) {
        removeMethod(module, methodName);
      }
    }
  }

  @JRubyMethod(required=2)
  public IRubyObject remove_method(IRubyObject class_name, IRubyObject method_name) {
    RubyModule module = wrapped.getModule(class_name.asJavaString());
    if (module != null) {
      removeMethod(module, method_name.asJavaString());
    }
    return runtime.getNil();
  }

  public IRubyObject remove_singleton_method(IRubyObject class_name, IRubyObject method_name) {
    RubyModule module = wrapped.getModule(class_name.asJavaString()).getSingletonClass();
    if (module != null) {
      removeMethod(module, method_name.asJavaString());
    }
    return runtime.getNil();
  }

  private void removeMethod(RubyModule module, String methodName) {
    // System.err.println("removing method " + methodName + " from " + module.inspect().asJavaString());
    module.removeMethod(runtime.getCurrentContext(), methodName);
  }

  @JRubyMethod(required=1)
  public IRubyObject eval(IRubyObject str) {
    try {
      return unbox(wrapped.evalScriptlet(str.asJavaString()));
    } catch(RaiseException e) {
      String msg = e.getException().callMethod(wrapped.getCurrentContext(), "message").asJavaString();
      String path = e.getException().type().getName();
      throw new RaiseException(runtime, rb_eSandboxException, path + ": " + msg, false);
    } catch(Exception e) {
      e.printStackTrace();
      runtime.getWarnings().warn(IRubyWarnings.ID.MISCELLANEOUS, "NativeException: " + e);
      return runtime.getNil();
    }
  }

  private IRubyObject unbox(IRubyObject boxed) {
    if (boxed.isImmediate()) {
      String dumped = wrapped.getModule("Marshal").callMethod(wrapped.getCurrentContext(), "dump", boxed).asJavaString();
      return runtime.getModule("Marshal").callMethod(runtime.getCurrentContext(), "load", runtime.newString(dumped));
    } else {
      return boxed; // TODO
    }
  }

  @JRubyMethod(required=1)
  public IRubyObject load(IRubyObject str) {
    // Not sure what the wrap argument does, using true for now
    wrapped.getLoadService().load(str.asJavaString(), true);
    return runtime.getTrue();
  }

  @JRubyMethod(required=1)
  public IRubyObject require(IRubyObject str) {
    try {
      return RubyKernel.require(wrapped.getKernel(), wrapped.newString(str.asJavaString()), Block.NULL_BLOCK);
    } catch(RaiseException e) {
      e.printStackTrace();
      return runtime.getFalse();
    }
  }
}
