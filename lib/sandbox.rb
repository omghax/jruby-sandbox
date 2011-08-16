require 'sandbox/sandbox'
require 'sandbox/version'

module Sandbox
  PRELUDE = File.expand_path('../sandbox/prelude.rb', __FILE__).freeze # :nodoc:

  class << self
    def new
      Full.new
    end

    def safe
      Safe.new
    end
  end

  class Safe < Full
  end
end
