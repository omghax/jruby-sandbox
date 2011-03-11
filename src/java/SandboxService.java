import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.runtime.load.BasicLibraryService;

import org.jruby.ext.sandbox.Sandbox;

public class SandboxService implements BasicLibraryService {
  public boolean basicLoad(Ruby runtime) throws IOException {
    Sandbox.initialize(runtime);
    return true;
  }
}