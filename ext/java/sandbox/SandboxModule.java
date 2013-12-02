package sandbox;

import org.jruby.Profile;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.builtin.IRubyObject;

public class SandboxModule {
  /**
   * Create the Sandbox module and add it to the Ruby runtime.
   */
  public static RubyModule createSandboxModule(final Ruby runtime) {
    RubyModule mSandbox = runtime.defineModule("Sandbox");
    mSandbox.defineAnnotatedMethods(SandboxModule.class);

    RubyClass cObject = runtime.getObject();
    RubyClass cSandboxFull = mSandbox.defineClassUnder("Full", cObject, SandboxFull.FULL_ALLOCATOR);
    cSandboxFull.defineAnnotatedMethods(SandboxFull.class);
    RubyClass cStandardError = runtime.getStandardError();
    RubyClass cSandboxException = mSandbox.defineClassUnder("SandboxException", cStandardError, cStandardError.getAllocator());

    return mSandbox;
  }

  @JRubyClass(name="Sandbox::SandboxException", parent="StandardError")
  public static class SandboxException {}

  @JRubyMethod(name="current", meta=true)
  public static IRubyObject s_current(IRubyObject recv) {
    Profile prof = recv.getRuntime().getProfile();
    if (prof instanceof SandboxProfile) {
      return ((SandboxProfile) prof).getSandbox();
    }
    return recv.getRuntime().getNil();
  }
}
