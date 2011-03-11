# -*- encoding: utf-8 -*-
$:.push File.expand_path('../lib', __FILE__)
require 'jruby_sandbox/version'

Gem::Specification.new do |s|
  s.name        = 'jruby_sandbox'
  s.version     = JRubySandbox::VERSION
  s.platform    = Gem::Platform::RUBY
  s.authors     = ['Dray Lacy']
  s.email       = ['dray@envylabs.com']
  s.homepage    = 'http://github.com/omghax/jruby_sandbox'
  s.summary     = %q{TODO: Write a gem summary}
  s.description = %q{TODO: Write a gem description}

  s.rubyforge_project = 'jruby_sandbox'

  s.files         = `git ls-files`.split("\n")
  s.test_files    = `git ls-files -- {test,spec,features}/*`.split("\n")
  s.executables   = `git ls-files -- bin/*`.split("\n").map{ |f| File.basename(f) }
  s.require_paths = ['lib']

  s.add_development_dependency 'rspec'
  s.add_development_dependency 'yard'
end