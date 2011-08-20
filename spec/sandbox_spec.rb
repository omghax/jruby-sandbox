require 'rspec'
require 'sandbox'

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
  end
end
