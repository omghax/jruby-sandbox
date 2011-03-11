require 'spec_helper'

describe Sandbox do
  let(:sandbox) { Sandbox.new }

  describe "#eval" do
    it "can define constants" do
      sandbox.eval('Foo = 1')
      sandbox.eval('Foo').should == 1
    end

    it "does not allow execution of system calls" do
      expect {
        sandbox.eval('`ls`')
      }.to raise_error(Sandbox::SandboxException, "NoMethodError: undefined method ``' for main:Object")
    end
  end

  describe "#require" do
    it "does not leak references into the host interpreter" do
      expect {
        sandbox.require('digest/md5')
      }.to_not change {
        Object.const_defined?(:Digest)
      }.from(false)
    end

    it "requires files into the sandboxed interpreter" do
      expect {
        sandbox.require('digest/md5')
      }.to change {
        sandbox.eval('Object.const_defined?(:Digest)')
      }.from(false).to(true)
    end
  end
end