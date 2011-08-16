package sandbox;

import org.jruby.Profile;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.builtin.IRubyObject;

public class SandboxModule {
  @JRubyMethod(name="current", meta=true)
  public static IRubyObject s_current(IRubyObject recv) {
    Profile prof = recv.getRuntime().getProfile();
    if (prof instanceof SandboxProfile) {
      return ((SandboxProfile) prof).getSandbox();
    }
    return recv.getRuntime().getNil();
  }
}
