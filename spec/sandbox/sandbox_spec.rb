require 'spec_helper'
require 'stringio'

describe Sandbox do
  let(:sandbox) { Sandbox.new }

  describe "#eval" do
    it "can define constants" do
      sandbox.eval('Foo = 1')
      1.should == sandbox.eval('Foo')
    end

    it "does not allow execution of system calls" do
      expect {
        sandbox.eval('`ls`')
      }.to raise_error(Sandbox::SandboxException, "NoMethodError: undefined method ``' for main:Object")
    end

    it "wraps exceptions from the sandbox" do
      expect {
        sandbox.eval("raise 'KABOOM'")
      }.to raise_error(Sandbox::SandboxException, 'RuntimeError: KABOOM')
    end

    # TODO: allowing Kernel#require is likely a security risk.
    it "does not leak references from required libraries" do
      expect {
        sandbox.eval("require 'digest/md5'")
      }.to_not change {
        Object.const_defined?(:Digest)
      }.from(false)
    end

    it "uses a persistent toplevel scope" do
      sandbox.eval('foo = 1')
      1.should == sandbox.eval('foo')
    end
  end

  describe "#last_result" do
    it "returns the result of the last expression executed" do
      sandbox.eval('foo = 1 + 2')
      3.should == sandbox.last_result
    end
  end
end
