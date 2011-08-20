package sandbox;

import org.jruby.Profile;
import org.jruby.runtime.builtin.IRubyObject;

public class SandboxProfile implements Profile {
  private IRubyObject sandbox;

  public SandboxProfile(IRubyObject sandbox) {
    this.sandbox = sandbox;
  }

  public IRubyObject getSandbox() {
    return sandbox;
  }

  public boolean allowBuiltin(String name) { return true; }
  public boolean allowClass(String name) { return true; }
  public boolean allowModule(String name) { return true; }
  public boolean allowLoad(String name) { return true; }
  public boolean allowRequire(String name) { return true; }
}
