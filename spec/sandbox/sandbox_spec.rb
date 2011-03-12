require 'spec_helper'
require 'stringio'

describe Sandbox::Interpreter do
  let(:sandbox) { Sandbox::Interpreter.new }

  describe "#eval" do
    it "can define constants" do
      sandbox.eval('Foo = 1')
      sandbox.eval('Foo').should == 1
    end

    def capture_output
      old_stdout = $stdout
      old_stderr = $stderr
      $stdout = StringIO.new
      $stderr = StringIO.new
      yield
      return $stdout, $stderr
    ensure
      $stdout = old_stdout
      $stderr = old_stderr
    end

    it "does not write to $stdout" do
      stdout, stderr = capture_output do
        sandbox.eval('$stdout.puts "hello"')
      end
      stdout.read.should == ''
    end

    it "does not write to $stderr" do
      stdout, stderr = capture_output do
        sandbox.eval('$stderr.puts "hello"')
      end
      stderr.read.should == ''
    end

    # it "does not allow execution of system calls" do
    #   expect {
    #     sandbox.eval('`ls`')
    #   }.to raise_error(Sandbox::SandboxException, "NoMethodError: undefined method ``' for main:Object")
    # end

    it "does not leak references from required libraries" do
      expect {
        sandbox.eval("require 'digest/md5'")
      }.to_not change {
        Object.const_defined?(:Digest)
      }.from(false)
    end
  end
end