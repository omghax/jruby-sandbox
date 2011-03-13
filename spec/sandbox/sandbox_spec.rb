require 'spec_helper'
require 'stringio'

describe Sandbox::Interpreter do
  let(:sandbox) { Sandbox::Interpreter.new }

  describe "#eval" do
    it "can define constants" do
      sandbox.eval('Foo = 1')
      1.should == sandbox.eval('Foo')
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
