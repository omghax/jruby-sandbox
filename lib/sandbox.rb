require "sandbox/sandbox"
require "sandbox/version"
require "sandbox/safe"

module Sandbox
  PRELUDE = File.expand_path("../sandbox/prelude.rb", __FILE__).freeze # :nodoc:

  class << self
    def new
      Full.new
    end

    def safe
      Safe.new
    end
  end
end
