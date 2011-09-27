require 'rspec'
require 'sandbox'
require 'timeout'

describe Sandbox do
  after(:each) do
    Object.class_eval { remove_const(:Foo) } if defined?(Foo)
  end

  describe ".new" do
    subject { Sandbox.new }

    it { should_not be_nil }
    it { should be_an_instance_of(Sandbox::Full) }
  end

  describe ".safe" do
    subject { Sandbox.safe }

    it { should be_an_instance_of(Sandbox::Safe) }

    it 'should not lock down until calling activate!' do
      subject.eval('`echo hello`').should == "hello\n"

      subject.activate!
        
      expect {
        subject.eval('`echo hello`')
      }.to raise_error(Sandbox::SandboxException)
    end
    
    it "should activate FakeFS inside the sandbox (and not allow it to be deactivated)" do
      subject.eval('File').should == ::File
      
      subject.activate!
      
      foo = File.join(File.dirname(__FILE__), 'support', 'foo.txt')

      expect {
        subject.eval(%{File.read('#{foo}')})
      }.to raise_error(Sandbox::SandboxException, /Errno::ENOENT: No such file or directory/)
      
      subject.eval('File').should == FakeFS::File
      subject.eval('Dir').should == FakeFS::Dir
      subject.eval('FileUtils').should == FakeFS::FileUtils
      subject.eval('FileTest').should == FakeFS::FileTest
      
      subject.eval(%{FakeFS.deactivate!})
      
      expect {
        subject.eval(%{File.read('#{foo}')})
      }.to raise_error(Sandbox::SandboxException, /Errno::ENOENT: No such file or directory/)
      
      subject.eval(%{File.open('/bar.txt', 'w') {|file| file << "bar" }})
      
      expect {
        subject.eval(%{FileUtils.cp('/bar.txt', '/baz.txt')})
      }.to_not raise_error(Sandbox::SandboxException, /NoMethodError/)
    end
  end

  describe ".current" do
    it "should not return a current sandbox outside a sandbox" do
      Sandbox.current.should be_nil
    end

    it "should return the current sandbox inside a sandbox" do
      pending do
        sandbox = Sandbox.new
        sandbox.ref(Sandbox)
        sandbox.eval('Sandbox.current').should == sandbox
      end
    end
  end
  
  describe "#eval with timeout" do
    subject { Sandbox.safe }
    
    context "before it's been activated" do
      it "should protect against long running code" do
        long_code = <<-RUBY
          sleep(5)
        RUBY

        expect {
          subject.eval(long_code, timeout: 1)
        }.to raise_error(Sandbox::TimeoutError)
      end
      
      it "should not raise a timeout error if the code runs in under the passed in time" do
        short_code = <<-RUBY
          1+1
        RUBY
        
        expect {
          subject.eval(short_code, timeout: 1)
        }.to_not raise_error(Sandbox::TimeoutError)
      end
    end
    
    context "after it's been activated" do
      before(:each) { subject.activate! }
      
      it "should protect against long running code" do
        long_code = <<-RUBY
          while true; end
        RUBY

        expect {
          subject.eval(long_code, timeout: 1)
        }.to raise_error(Sandbox::TimeoutError)
      end
      
      it "should persist state between evaluations" do
        subject.eval('o = Object.new', timeout: 1)
        
        expect {
          subject.eval('o', timeout: 1)
        }.to_not raise_error(Sandbox::SandboxException)
      end
    end
  end

  describe "#eval" do
    subject { Sandbox.new }

    it "should allow a range of common operations" do
      operations = <<-OPS
        1 + 1
        'foo'.chomp
        'foo'
      OPS
      subject.eval(operations).should == 'foo'
    end
    
    it "should have an empty ENV" do
      pending do
        subject.eval(%{ENV.to_a}).should be_empty 
      end
    end

    it "should persist state between evaluations" do
      subject.eval('o = Object.new')
      subject.eval('o').should_not be_nil
    end

    it "should be able to define a new class in the sandbox" do
      result = subject.eval('Foo = Struct.new(:foo); struct = Foo.new("baz"); struct.foo')
      result.should == 'baz'
    end

    it "should be able to use a class across invocations" do
      # Return nil, because the environment doesn't know "Foo"
      subject.eval('Foo = Struct.new(:foo); nil')
      subject.eval('struct = Foo.new("baz"); nil')
      subject.eval('struct.foo').should == 'baz'
    end

    describe "communication between sandbox and environment" do
      it "should be possible to pass data from the box to the environment" do
        Foo = Struct.new(:foo)
        subject.ref(Foo)
        struct = subject.eval('struct = Foo.new')
        subject.eval('struct.foo = "baz"')
        struct.foo.should == 'baz'
      end

      it "should be possible to pass data from the environment to the box" do
        Foo = Struct.new(:foo)
        subject.ref(Foo)
        struct = subject.eval('struct = Foo.new')
        struct.foo = 'baz'
        subject.eval('struct.foo').should == 'baz'
      end

      it "should be able to pass large object data from the box to the environment" do
        expect {
          subject.eval %{
            (0..1000).to_a.inject({}) {|h,i| h[i] = "HELLO WORLD"; h }
          }
        }.to_not raise_error(Sandbox::SandboxException)

        expect {
          subject.eval %{'RUBY'*100}
        }.to_not raise_error(Sandbox::SandboxException)
      end
    end
  end
  
  describe "#import" do
    subject { Sandbox.new }
    
    it "should be able to call a referenced namespaced module method" do
      Foo = Class.new
      Foo::Bar = Module.new do
        def baz
          'baz'
        end
        module_function :baz
      end

      subject.import(Foo::Bar)
      subject.eval('Foo::Bar.baz').should == 'baz'
    end

    it "should be able to include a module from the environment" do
      Foo = Module.new do
        def baz
          'baz'
        end
      end

      subject.import(Foo)
      subject.eval("class Bar; include Foo; end; nil")
      subject.eval('Bar.new.baz').should == 'baz'
    end
    
    it "should be able to copy instance methods from a module that uses module_function" do
      Foo = Module.new do        
        def baz; 'baz'; end
        
        module_function :baz
      end
      
      subject.import Foo
      subject.eval('Foo.baz').should == 'baz'
    end
  end

  describe "#ref" do
    subject { Sandbox.new }

    it "should be possible to reference a class defined outside the box" do
      Foo = Class.new
      subject.ref(Foo)
      subject.eval('Foo.new').should be_an_instance_of(Foo)
    end

    it "should be possible to change the class after the ref" do
      Foo = Class.new
      subject.ref(Foo)
      def Foo.foo; 'baz'; end
      subject.eval('Foo.foo').should == 'baz'
    end

    it "should be possible to dynamically add a class method after the ref" do
      Foo = Class.new
      subject.ref(Foo)
      Foo.class_eval('def Foo.foo; "baz"; end')
      subject.eval('Foo.foo').should == 'baz'
    end

    it "should be possible to dynamically add a class method after the ref" do
      Foo = Class.new
      subject.ref(Foo)
      Foo.instance_eval('def Foo.foo; "baz"; end')
      subject.eval('Foo.foo').should == 'baz'
    end

    it "should be possible to call a method on the class that receives a block" do
      Foo = Class.new do
        def self.bar
          yield
        end
      end
      subject.ref(Foo)
      subject.eval(%{Foo.bar { "baz" }}).should == 'baz'
    end
  end
end
