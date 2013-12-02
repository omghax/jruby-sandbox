package sandbox;

import java.util.Collection;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyInstanceConfig;
import org.jruby.RubyKernel;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.CompatVersion;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.common.IRubyWarnings;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.DynamicScope;


@JRubyClass(name="Sandbox::Full")
public class SandboxFull extends RubyObject {
  protected static ObjectAllocator FULL_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new SandboxFull(runtime, klass);
    }
  };

  private Ruby wrapped;
  private DynamicScope currentScope;

  protected SandboxFull(Ruby runtime, RubyClass type) {
    super(runtime, type);
    reload();
  }

  @JRubyMethod
  public IRubyObject reload() {
    RubyInstanceConfig cfg = new RubyInstanceConfig();
    cfg.setObjectSpaceEnabled(getRuntime().getInstanceConfig().isObjectSpaceEnabled());
    cfg.setInput(getRuntime().getInstanceConfig().getInput());
    cfg.setOutput(getRuntime().getInstanceConfig().getOutput());
    cfg.setError(getRuntime().getInstanceConfig().getError());
    cfg.setCompatVersion(CompatVersion.RUBY1_9);
    cfg.setScriptFileName("(sandbox)");

    SandboxProfile profile = new SandboxProfile(this);
    cfg.setProfile(profile);

    wrapped = Ruby.newInstance(cfg);
    currentScope = wrapped.getCurrentContext().getCurrentScope();

    BoxedClass.createBoxedClassClass(wrapped);

    return this;
  }

  @JRubyMethod(required=1)
  public IRubyObject eval(IRubyObject str) {
    try {
      IRubyObject result = wrapped.evalScriptlet(str.asJavaString(), currentScope);
      return unbox(result);
    } catch (RaiseException e) {
      String msg = e.getException().callMethod(wrapped.getCurrentContext(), "message").asJavaString();
      String path = e.getException().type().getName();
      RubyClass eSandboxException = (RubyClass) getRuntime().getClassFromPath("Sandbox::SandboxException");
      throw new RaiseException(getRuntime(), eSandboxException, path + ": " + msg, false);
    } catch (Exception e) {
      e.printStackTrace();
      getRuntime().getWarnings().warn(IRubyWarnings.ID.MISCELLANEOUS, "NativeException: " + e);
      return getRuntime().getNil();
    }
  }

  @JRubyMethod(name="import", required=1)
  public IRubyObject _import(IRubyObject klass) {
    if (!(klass instanceof RubyModule)) {
      throw getRuntime().newTypeError(klass, getRuntime().getClass("Module"));
    }
    String name = ((RubyModule) klass).getName();
    importClassPath(name, false);
    return getRuntime().getNil();
  }

  @JRubyMethod(required=1)
  public IRubyObject ref(IRubyObject klass) {
    if (!(klass instanceof RubyModule)) {
      throw getRuntime().newTypeError(klass, getRuntime().getClass("Module"));
    }
    String name = ((RubyModule) klass).getName();
    importClassPath(name, true);
    return getRuntime().getNil();
  }

  private RubyModule importClassPath(String path, final boolean link) {
    RubyModule runtimeModule = getRuntime().getObject();
    RubyModule wrappedModule = wrapped.getObject();

    if (path.startsWith("#")) {
      throw getRuntime().newArgumentError("can't import anonymous class " + path);
    }

    for (String name : path.split("::")) {
      runtimeModule = (RubyModule) runtimeModule.getConstantAt(name);
      // Create the module when it did not exist yet...
      if (wrappedModule.const_defined_p(wrapped.getCurrentContext(), wrapped.newString(name)).isFalse()) {
        // The BoxedClass takes the place of Object as top of the inheritance
        // hierarchy. As a result, we can intercept all new instances that are
        // created and all method_missing calls.
        RubyModule sup = wrapped.getClass("BoxedClass");
        if (!link && runtimeModule instanceof RubyClass) {
          // If we're importing a class, recursively import all of its
          // superclasses as well.
          sup = importClassPath(runtimeModule.getSuperClass().getName(), true);
        }
        
        RubyClass klass = (RubyClass) sup;
        if (wrappedModule == wrapped.getObject()) {
          
          if (link || runtimeModule instanceof RubyClass) { // if this is a ref and not an import
            wrappedModule = wrapped.defineClass(name, klass, klass.getAllocator());
          } else {
            wrappedModule = wrapped.defineModule(name);
          }
          
        } else {
          if (runtimeModule instanceof RubyClass) {
            wrappedModule = wrappedModule.defineClassUnder(name, klass, klass.getAllocator());
          } else {
            wrappedModule = wrappedModule.defineModuleUnder(name);
          }
          
        }
      } else {
        // ...or just resolve it, if it was already known
        wrappedModule = (RubyModule) wrappedModule.getConstantAt(name);
      }
      
      // Check the consistency of the hierarchy
      if (runtimeModule instanceof RubyClass) {
        if (!link && !runtimeModule.getSuperClass().getName().equals(wrappedModule.getSuperClass().getName())) {
          throw getRuntime().newTypeError("superclass mismatch for class " + runtimeModule.getSuperClass().getName());
        }
      }

      if (link || runtimeModule instanceof RubyClass) {
        linkObject(runtimeModule, wrappedModule);
      } else {
        copyMethods(runtimeModule, wrappedModule);
      }
    }

    return runtimeModule;
  }

  private void copyMethods(RubyModule from, RubyModule to) {
    to.getMethodsForWrite().putAll(from.getMethods());
    to.getSingletonClass().getMethodsForWrite().putAll(from.getSingletonClass().getMethods());
  }

  @JRubyMethod(required=2)
  public IRubyObject keep_methods(IRubyObject className, IRubyObject methods) {
    RubyModule module = wrapped.getModule(className.asJavaString());
    if (module != null) {
      keepMethods(module, methods.convertToArray());
    }
    return methods;
  }

  @JRubyMethod(required=2)
  public IRubyObject keep_singleton_methods(IRubyObject className, IRubyObject methods) {
    RubyModule module = wrapped.getModule(className.asJavaString()).getSingletonClass();
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
  public IRubyObject remove_method(IRubyObject className, IRubyObject methodName) {
    RubyModule module = wrapped.getModule(className.asJavaString());
    if (module != null) {
      removeMethod(module, methodName.asJavaString());
    }
    return getRuntime().getNil();
  }

  @JRubyMethod(required=2)
  public IRubyObject remove_singleton_method(IRubyObject className, IRubyObject methodName) {
    RubyModule module = wrapped.getModule(className.asJavaString()).getSingletonClass();
    if (module != null) {
      removeMethod(module, methodName.asJavaString());
    }
    return getRuntime().getNil();
  }

  private void removeMethod(RubyModule module, String methodName) {
    // System.err.println("removing method " + methodName + " from " + module.inspect().asJavaString());
    module.removeMethod(wrapped.getCurrentContext(), methodName);
  }

  @JRubyMethod(required=1)
  public IRubyObject load(IRubyObject str) {
    try {
      wrapped.getLoadService().load(str.asJavaString(), true);
      return getRuntime().getTrue();
    } catch (RaiseException e) {
      e.printStackTrace();
      return getRuntime().getFalse();
    }
  }

  @JRubyMethod(required=1)
  public IRubyObject require(IRubyObject str) {
    try {
      IRubyObject result = RubyKernel.require(wrapped.getKernel(), wrapped.newString(str.asJavaString()), Block.NULL_BLOCK);
      return unbox(result);
    } catch (RaiseException e) {
      e.printStackTrace();
      return getRuntime().getFalse();
    }
  }

  private IRubyObject unbox(IRubyObject obj) {
    return box(obj);
  }

  private IRubyObject rebox(IRubyObject obj) {
    return box(obj);
  }

  private IRubyObject box(IRubyObject obj) {
    if (obj.isImmediate()) {
      return cross(obj);
    } else {
      // If this object already existed and was returned from the wrapped
      // runtime on an earlier occasion, it will already contain a link to its
      // brother in the regular runtime and we can safely return that link.
      IRubyObject link = getLinkedObject(obj);
      if (!link.isNil()) {
        IRubyObject box = getLinkedBox(obj);
        if (box == this) return link;
      }

      // Is the class already known on both sides of the fence?
      IRubyObject klass = constFind(obj.getMetaClass().getRealClass().getName());
      link = getRuntime().getNil();
      if (!klass.isNil()) {
        link = getLinkedObject(klass);
      }

      if (link.isNil()) {
        return cross(obj);
      } else {
        IRubyObject v = ((RubyClass)klass).allocate();
        linkObject(obj, v);
        return v;
      }
    }
  }

  private IRubyObject cross(IRubyObject obj) {
    IRubyObject dumped = wrapped.getModule("Marshal").callMethod(wrapped.getCurrentContext(), "dump", obj);
    return getRuntime().getModule("Marshal").callMethod(getRuntime().getCurrentContext(), "load", dumped);
  }

  protected static IRubyObject getLinkedObject(IRubyObject arg) {
    IRubyObject object = arg.getRuntime().getNil();
    if (arg.getInstanceVariables().hasInstanceVariable("__link__")) {
      object = (IRubyObject) arg.getInstanceVariables().getInstanceVariable("__link__");
    }
    return object;
  }

  protected static IRubyObject getLinkedBox(IRubyObject arg) {
    IRubyObject object = arg.getRuntime().getNil();
    if (arg.getInstanceVariables().hasInstanceVariable("__box__")) {
      object = (IRubyObject) arg.getInstanceVariables().getInstanceVariable("__box__");
    }
    return object;
  }

  private void linkObject(IRubyObject runtimeObject, IRubyObject wrappedObject) {
    wrappedObject.getInstanceVariables().setInstanceVariable("__link__", runtimeObject);
    wrappedObject.getInstanceVariables().setInstanceVariable("__box__", this);
  }

  private IRubyObject constFind(String path) {
    try {
      return wrapped.getClassFromPath(path);
    } catch (Exception e) {
      return wrapped.getNil();
    }
  }

  protected IRubyObject runMethod(IRubyObject recv, String name, IRubyObject[] args, Block block) {
    IRubyObject[] args2 = new IRubyObject[args.length];
    for (int i = 0; i < args.length; i++) {
      args2[i] = unbox(args[i]);
    }
    IRubyObject recv2 = unbox(recv);
    IRubyObject result = recv2.callMethod(getRuntime().getCurrentContext(), name, args2, block);
    return rebox(result);
  }
}
