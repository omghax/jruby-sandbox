import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.runtime.load.BasicLibraryService;

public class SandKitService implements BasicLibraryService {
  public boolean basicLoad(Ruby runtime) throws IOException {
    SandKit.initialize(runtime);
    return true;
  }
}
