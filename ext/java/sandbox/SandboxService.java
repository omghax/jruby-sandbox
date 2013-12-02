package sandbox;

import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.runtime.load.BasicLibraryService;

public class SandboxService implements BasicLibraryService {
  public boolean basicLoad(Ruby runtime) throws IOException {
    init(runtime);
    return true;
  }

  private void init(Ruby runtime) {
    SandboxModule.createSandboxModule(runtime);
  }
}
