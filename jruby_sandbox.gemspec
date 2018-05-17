# -*- encoding: utf-8 -*-
$:.push File.expand_path("../lib", __FILE__)
require "sandbox/version"

Gem::Specification.new do |s|
  s.name        = "xplenty-jruby_sandbox"
  s.version     = Sandbox::VERSION
  s.platform    = "java"
  s.authors     = ["Dray Lacy", "Eric Allam", "Moty Michaely"]
  s.email       = ["dray@envylabs.com", "eric@envylabs.com", "moty.mi@gmail.com"]
  s.homepage    = "http://github.com/xplenty/jruby-sandbox"
  s.summary     = "Sandbox support for JRuby"
  s.description = "A version of _why's Freaky Freaky Sandbox for JRuby."

  s.rubyforge_project = "jruby_sandbox"

  s.files         = `git ls-files`.split("\n") + ["lib/sandbox/sandbox.jar"]
  s.test_files    = `git ls-files -- {test,spec,features}/*`.split("\n")
  s.executables   = `git ls-files -- bin/*`.split("\n").map{ |f| File.basename(f) }
  s.require_paths = ["lib"]

  s.add_dependency "fakefs", "< 0.14.0"

  s.add_development_dependency "rake"
  s.add_development_dependency "rake-compiler"
  s.add_development_dependency "rspec"
  s.add_development_dependency "yard"
end
