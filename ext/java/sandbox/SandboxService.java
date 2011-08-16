package sandbox;

import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.BasicLibraryService;

public class SandboxService implements BasicLibraryService {
  public boolean basicLoad(Ruby runtime) throws IOException {
    init(runtime);
    return true;
  }

  private void init(Ruby runtime) {
    RubyModule mSandbox = runtime.defineModule("Sandbox");
    mSandbox.defineAnnotatedMethods(SandboxModule.class);

    RubyClass cSandboxFull = mSandbox.defineClassUnder("Full", runtime.getObject(), FULL_ALLOCATOR);
    cSandboxFull.defineAnnotatedMethods(SandboxFull.class);

    RubyClass cSandboxException = mSandbox.defineClassUnder("SandboxException", runtime.getException(), runtime.getException().getAllocator());
  }

  protected static final ObjectAllocator FULL_ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby runtime, RubyClass klass) {
      return new SandboxFull(runtime, klass);
    }
  };
}
