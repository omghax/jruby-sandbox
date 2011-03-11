package org.jruby.ext.sandbox;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.util.Arrays;
import java.util.Collections;

import org.jruby.Ruby;
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

@JRubyClass(name="Sandbox")
public class Sandbox extends RubyObject {
  private final static ObjectAllocator SANDBOX_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new Sandbox(runtime, klass);
    }
  };

  public static void initialize(Ruby runtime) {
    rb_cSandbox = runtime.defineClass("Sandbox", runtime.getObject(), SANDBOX_ALLOCATOR);
    rb_cSandbox.defineAnnotatedMethods(Sandbox.class);

    rb_eStandardError = runtime.getStandardError();
    rb_eSandboxException = rb_cSandbox.defineClassUnder("SandboxException", rb_eStandardError, rb_eStandardError.getAllocator());
  }

  // TODO remove these
  private static RubyClass rb_cSandbox;
  private static RubyClass rb_eStandardError;
  private static RubyClass rb_eSandboxException;

  private Ruby runtime;
  private Ruby wrapped;

  public Sandbox(Ruby runtime, RubyClass klass) {
    super(runtime, klass);
    this.runtime = runtime;
    this.wrapped = initWrapped();

    removeMethods();
    removeSingletonMethods();
  }

  private Ruby initWrapped() {
    RubyInstanceConfig cfg = new RubyInstanceConfig();
    cfg.setInput(runtime.getInstanceConfig().getInput());
    cfg.setOutput(runtime.getInstanceConfig().getOutput());
    cfg.setError(runtime.getInstanceConfig().getError());
    cfg.setObjectSpaceEnabled(runtime.getInstanceConfig().isObjectSpaceEnabled());
    Ruby wrapped = Ruby.newInstance(cfg);
    // wrapped.getLoadService().load(rb_cSandbox.getConstant("PRELUDE").asJavaString(), true);
    return wrapped;
  }

  public static final Map<String, Set<String>> METHODS = new HashMap<String, Set<String>>();
  static {
    METHODS.put("Kernel", new HashSet<String>(Arrays.asList(new String[]{
        "nil?"
      , "=="
      , "equal?"
      , "==="
      , "=~"
      , "eql?"
      , "id"
      , "type"
      , "class"
      , "clone"
      , "dup"
      , "initialize_copy"
      , "taint"
      , "tainted?"
      , "untaint"
      , "freeze"
      , "frozen?"
      , "to_a"
      , "hash"
      , "to_s"
      , "inspect"
      , "methods"
      , "singleton_methods"
      , "protected_methods"
      , "private_methods"
      , "public_methods"
      , "instance_variables"
      , "instance_variable_get"
      , "instance_variable_set"
      , "remove_instance_variable"
      , "instance_of?"
      , "kind_of?"
      , "is_a?"
      , "singleton_method_added"
      , "singleton_method_removed"
      , "singleton_method_undefined"
      , "respond_to?"
      , "send"
      , "__send__"
      , "instance_eval"
      , "sprintf"
      , "format" 
      , "Integer"
      , "Float"
      , "String"
      , "Array"
      , "eval"
      , "iterator?"
      , "block_given?"
      , "method_missing"
      , "loop"
      , "raise"
      , "fail"
      , "catch"
      , "throw"
      , "global_variables"
      , "local_variables"
      , "sub"
      , "gsub"
      , "sub!"
      , "gsub!"
      , "chop"
      , "chop!"
      , "chomp"
      , "chomp!"
      , "split"
      , "scan"
      , "proc"
      , "lambda"
      , "binding"
    })));
    METHODS.put("NilClass", new HashSet<String>(Arrays.asList(new String[]{
        "to_i"
      , "to_f"
      , "to_s"
      , "to_a"
      , "inspect"
      , "&"
      , "|"
      , "^"
      , "nil?"
    })));
    METHODS.put("Symbol", new HashSet<String>(Arrays.asList(new String[]{
        "to_i"
      , "to_int"
      , "inspect"
      , "to_s"
      , "id2name"
      , "to_sym"
      , "==="
    })));
    METHODS.put("TrueClass", new HashSet<String>(Arrays.asList(new String[]{
        "to_s"
      , "&"
      , "|"
      , "^"
    })));
    METHODS.put("FalseClass", new HashSet<String>(Arrays.asList(new String[]{
        "to_s"
      , "&"
      , "|"
      , "^"
    })));
    METHODS.put("Enumerable", new HashSet<String>(Arrays.asList(new String[]{
        "to_a"
      , "entries"
      , "sort"
      , "sort_by"
      , "grep"
      , "find"
      , "detect"
      , "find_all"
      , "select"
      , "reject"
      , "collect"
      , "map"
      , "inject"
      , "partition"
      , "all?"
      , "any?"
      , "min"
      , "max"
      , "member?"
      , "include?"
      , "each_with_index"
      , "zip"
    })));
    METHODS.put("String", new HashSet<String>(Arrays.asList(new String[]{
        "initialize"
      , "initialize_copy"
      , "<=>"
      , "=="
      , "eql?"
      , "hash"
      , "casecmp"
      , "+"
      , "*"
      , "%"
      , "[]"
      , "[]="
      , "insert"
      , "length"
      , "size"
      , "empty?"
      , "=~"
      , "match"
      , "succ"
      , "succ!"
      , "next"
      , "next!"
      , "upto"
      , "index"
      , "rindex"
      , "replace"

      , "to_i"
      , "to_f"
      , "to_s"
      , "to_str"
      , "inspect"
      , "dump"

      , "upcase"
      , "downcase"
      , "capitalize"
      , "swapcase"

      , "upcase!"
      , "downcase!"
      , "capitalize!"
      , "swapcase!"

      , "hex"
      , "oct"
      , "split"
      , "reverse"
      , "reverse!"
      , "concat"
      , "<<"
      , "crypt"
      , "intern"
      , "to_sym"

      , "include?"

      , "scan"

      , "ljust"
      , "rjust"
      , "center"

      , "sub"
      , "gsub"
      , "chop"
      , "chomp"
      , "strip"
      , "lstrip"
      , "rstrip"

      , "sub!"
      , "gsub!"
      , "chop!"
      , "chomp!"
      , "strip!"
      , "lstrip!"
      , "rstrip!"

      , "tr"
      , "tr_s"
      , "delete"
      , "squeeze"
      , "count"

      , "tr!"
      , "tr_s!"
      , "delete!"
      , "squeeze!"

      , "each_line"
      , "each"
      , "each_byte"

      , "sum"

      , "slice"
      , "slice!"
    })));
  }

  private void removeMethods() {
    for (Map.Entry<String, Set<String>> entry : METHODS.entrySet()) {
      String className = entry.getKey();
      Set retain = entry.getValue();
      RubyModule module = wrapped.getModule(className);
      if (module != null) {
        for (Map.Entry<String, DynamicMethod> methodEntry : module.getMethods().entrySet()) {
          String methodName = methodEntry.getKey();
          if (!retain.contains(methodName)) {
            // System.err.println("removing method " + methodName + " from " + className);
            module.removeMethod(runtime.getCurrentContext(), methodName);
          }
        }
      }
    }
  }

  public static final Map<String, Set<String>> SINGLETON_METHODS = new HashMap<String, Set<String>>();
  static {
    SINGLETON_METHODS.put("Kernel", new HashSet<String>(Arrays.asList(new String[]{
        "sprintf"
      , "format" 
      , "Integer"
      , "Float"
      , "String"
      , "Array"
      , "eval"
      , "iterator?"
      , "block_given?"
      , "method_missing"
      , "loop"
      , "raise"
      , "fail"
      , "catch"
      , "throw"
      , "global_variables"
      , "local_variables"
      , "sub"
      , "gsub"
      , "sub!"
      , "gsub!"
      , "chop"
      , "chop!"
      , "chomp"
      , "chomp!"
      , "split"
      , "scan"
      , "proc"
      , "lambda"
      , "binding"
    })));
    SINGLETON_METHODS.put("Symbol", new HashSet<String>(Arrays.asList(new String[]{"all_symbols"})));
    SINGLETON_METHODS.put("String", new HashSet<String>(Arrays.asList(new String[]{"new"})));
  }

  private void removeSingletonMethods() {
    for (Map.Entry<String, Set<String>> entry : SINGLETON_METHODS.entrySet()) {
      String className = entry.getKey();
      Set retain = entry.getValue();
      RubyModule module = wrapped.getModule(className).getSingletonClass();
      if (module != null) {
        for (Map.Entry<String, DynamicMethod> methodEntry : module.getMethods().entrySet()) {
          String methodName = methodEntry.getKey();
          if (!retain.contains(methodName)) {
            // System.err.println("removing singleton method " + methodName + " from " + className);
            module.removeMethod(runtime.getCurrentContext(), methodName);
          }
        }
      }
    }
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
  public IRubyObject require(IRubyObject str) {
    return RubyKernel.require(wrapped.getKernel(), wrapped.newString(str.asJavaString()), Block.NULL_BLOCK);
  }
}